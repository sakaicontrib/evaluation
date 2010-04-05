package org.sakaiproject.evaluation.logic.scheduling;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalTaskStatusEntry;
import org.sakaiproject.evaluation.model.EvalTaskStatusStream;
import org.sakaiproject.evaluation.model.EvalTaskStreamContainer;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;
import org.sakaiproject.taskstream.domain.TaskStatusStandardValues;

/**
 * This U-M class runs under Quartz and sends an email summary report on background tasks
 * of importing data in files and sending single-email announcements and reminders.
 * It depends on a U-M Task Status Servlet (Restlet).
 * 
 * @author rwellis
 *
 */
public class EvalStatusEmailImpl implements Job{
	
	private static Log log = LogFactory.getLog(EvalStatusEmailImpl.class);
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}
	
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	private final static String ANNOUNCEMENTS = "announcements"; // entryTag announcements step summary
	private final static String ANNOUNCEMENT_GROUPS = "announcementGroups"; // entryTag announcement groups step summary
	private final static String ANNOUNCEMENT_USERS = "announcementUsers"; // entryTag announcement users step summary
	private final static String CONTAINER_OTYPE = ".TQ.Container"; // XML normalized re the default container XML
	private final static String ERROR = "error"; // entryTag for problems
	private final static String FILES_WITH_ERROR_OTYPE = ".TQ.FilesWError"; //detail of files reporting problems
	private final static String FILES_WITHOUT_ERROR_OTYPE = ".TQ.FilesWOError"; //detail of files imported successfuly
	private final static String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"; // since query parameter date pattern
	private final static String EMAIL_STREAM_TAG = "Email"; // streamTag for single email jobs
	private final static String IMPORT_STREAM_TAG = "Import"; // streamTag for import servlet files
	private final static String REMINDERS = "reminders"; // entryTag reminders step summary
	private final static String REMINDER_GROUPS = "reminderGroups"; // entryTag reminder groups step summary
	private final static String REMINDER_USERS = "reminderUsers"; // entryTag reminder users step summary
	private final static String STATUS_STREAM_TAG = "StatusReport"; // streamTag for EvalStatusEmailImpl
	private final static String SUMMARY = "summary"; // entryTag for job summary
	private final static String WITH_ERROR = "withError"; // entryTag task ran to completion  with error
	private final static String WITHOUT_ERROR = "withoutError"; // entryTag task ran to completion without error
	
	public void init() {
	}

	/**
	 * The main method of a Quartz job. 
	 * Get data from background tasks to report to an email distribution list.
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if(log.isInfoEnabled()) log.info(this + ".execute - start.");
			
			// CREATED
			String streamUrl = evaluationService.newTaskStatusStream(STATUS_STREAM_TAG);
			
			// RUNNING
			String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
					null, TaskStatusStandardValues.RUNNING, null);
			
			if(log.isDebugEnabled()) log.debug("stream tag " + STATUS_STREAM_TAG + " streamUrl " + streamUrl);
			
			//since the last time this job was run
			DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
			Date date = (Date) evalSettings.get(EvalSettings.STATUS_SINCE_DATE);
			String since = formatter.format(date);
			
			String taskStatusUrl = evaluationService.getTaskStatusUrl();
			String queryParameters = null;
			String filesText = null;
			
			// EMAIL JOB
			String baseEmailQuery = "?streamTag=" + EMAIL_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=2";
			String emailErrorQuery = "?streamTag=" + EMAIL_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0" + "&entryTag=error";
			List<String> ranText = new ArrayList<String>();
			Integer filesUnfinished = null;
			String anc = null, ancGroups = null, ancUsers = null;
			String rem = null, remGroups = null, remUsers = null;
			String email = null;

			// did the email job run
			Boolean jobRan = emailJobRan(baseEmailQuery, streamUrl);
			Boolean jobErrors = jobErrors(emailErrorQuery, streamUrl);
			if(jobRan) {
				EvalTaskStreamContainer container = evaluationService.getTaskStatusContainer(baseEmailQuery);
				// FOR EACH EMAIL JOB - typically 1 a day
				for(EvalTaskStatusStream stream : container.getStreams()) {
					anc = "Announcements were not sent.";
					rem = "Reminders were not sent";
					for(EvalTaskStatusEntry entry : stream.getStatusEntries()) {
						if(ANNOUNCEMENTS.equalsIgnoreCase(entry.getEntryTag())) {
							anc = "Announcements were sent. This took " + entry.getPayload() + ".";
						}
						else if(ANNOUNCEMENT_GROUPS.equalsIgnoreCase(entry.getEntryTag())) {
							ancGroups = "Announcements were sent to " + entry.getPayload() + " groups.";
						}
						else if (ANNOUNCEMENT_USERS.equalsIgnoreCase(entry.getEntryTag())) {
							ancUsers = "Announcements were sent to " + entry.getPayload() + " users.";
						}
						else if(REMINDERS.equalsIgnoreCase(entry.getEntryTag())) {
							rem = "Reminders were sent. This took "  + entry.getPayload() + ".";
						}
						else if (REMINDER_GROUPS.equalsIgnoreCase(entry.getEntryTag())) {
							remGroups = "Reminders were sent to " + entry.getPayload() + " groups.";
						}
						else if(REMINDER_USERS.equalsIgnoreCase(entry.getEntryTag())) {
							remUsers = "Reminders were sent to " + entry.getPayload() + " users.";
						}
						else if(SUMMARY.equals(entry.getEntryTag())) {
							email = entry.getPayload();
						}
					}
					addEmailText(ranText, anc, ancGroups, ancUsers, rem,
							remGroups, remUsers, email, taskStatusUrl, jobErrors, since);
				}
			}
			
			// IMPORT - (1 stream per file imported)
			String baseImportQuery = "?streamTag=" + IMPORT_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0";
			List<String> loadedText = new ArrayList<String>();
			Integer filesTotal = importedTotal(baseImportQuery, streamUrl);
			Boolean dataLoaded = new Boolean(false);
			if(filesTotal > 0) 
			{
				// get the details of files loaded
				dataLoaded = Boolean.TRUE;
				// all files loaded
				filesText =  " files were ";
				if(filesTotal == 1) {
					filesText =  " file was ";
				}
				loadedText.add(filesTotal.toString() + filesText + "loaded since the last email status report.");
				String containerQuery = "?streamTag=Import&since=" + since + "&depth=2" + "&otype=" + CONTAINER_OTYPE + "&streamStatus=" + TaskStatusStandardValues.FINISHED;
				EvalTaskStatusStream firstStream = null;
				EvalTaskStatusStream lastStream = null;
				// TODO need the first and last streams not the whole container
				EvalTaskStreamContainer container = evaluationService.getTaskStatusContainer(containerQuery);
				if(container.getStreamCount().intValue() > 1) {
					firstStream = container.getStreams().get(0);
					lastStream = container.getStreams().get(container.getStreamCount() - 1);
					// by convention the second entry payload has the filename
					EvalTaskStatusEntry secondEntry = firstStream.getStatusEntries().get(1);
					// first file
					loadedText.add("The first file " + secondEntry.getPayload() + " started loading at " + secondEntry.getCreated());
					// by convention the last entry payload has the filename
					EvalTaskStatusEntry lastEntry = lastStream.getStatusEntries().get(lastStream.getStatusEntries().size() - 1);
					// last file
					loadedText.add("The last file " + lastEntry.getPayload() + " finished loading at " + lastEntry.getCreated());
				}

				// files without errors
				Integer filesWithoutError = importedWithoutError(baseImportQuery, streamUrl);
				if(filesWithoutError > 0) {
					if(filesWithoutError == 1) {
						loadedText.add("There was " + filesWithoutError.toString() + " file successfully loaded.");
					}
					else {
						loadedText.add("There were " + filesWithoutError.toString() + " files successfully loaded.");
					}
					// link to details
					queryParameters = "?otype=" + FILES_WITHOUT_ERROR_OTYPE
							+ "&streamTag=" + IMPORT_STREAM_TAG 
							+ "&since=" + since 
							+ "&entryTag=" + WITHOUT_ERROR
							+ "&entryStatus=" + TaskStatusStandardValues.FINISHED;
					loadedText.add("\nDetails are <a href='" + taskStatusUrl + queryParameters + "'>here</a>.");
				}
				// files with errors
				Integer filesWithError = importedWithError(baseImportQuery, streamUrl);
				if(filesWithError > 0) {
					if(filesWithError == 1) {
						loadedText.add("There was " + filesWithError.toString() + " file that reported problems.");
					}
					else {
						loadedText.add("There were " + filesWithError.toString() + " files that reported problems.");
					}
					// link to details
					queryParameters = "?otype=" + FILES_WITH_ERROR_OTYPE
							+ "&streamTag=" + IMPORT_STREAM_TAG 
							+ "&since=" + since 
							+ "&entryTag=" + WITH_ERROR
							+ "&entryStatus=" + TaskStatusStandardValues.FINISHED;
					loadedText.add("\nDetails are <a href='"  + taskStatusUrl + queryParameters + "'>here</a>.");
				}
				// files still being processed
				filesUnfinished = importUnfinished(baseImportQuery, streamUrl);
				if(filesUnfinished > 0) {
					filesText = " files have ";
					if(filesUnfinished == 1) {
						filesText = " file has ";
					}
					loadedText.add(filesUnfinished.toString() + filesText + "not finished processing.");
				}
			}
			
			// send the task status email report
			emails.sendEvalTaskStatusEmail(jobRan,dataLoaded, ranText, loadedText, filesUnfinished);
			
			// update since
			Date newDate = updateSince();
			
			if(log.isDebugEnabled()) log.debug("stream tag " + STATUS_STREAM_TAG + " entryUrl " + entryUrl);
			
			if(log.isInfoEnabled()) log.info(this + ".execute - done.");
			
			// FINISHED
			entryUrl = evaluationService.newTaskStatusEntry(streamUrl, "STATUS_SINCE_DATE", 
					TaskStatusStandardValues.FINISHED, "updated to " + newDate.toString());
		
		} catch (Exception e) {
			log.error("EvalStatusEmailImpl.execute " + e);
		}
		finally {
			// TODO close task status stream here
		}
	}

	private Boolean jobErrors(String emailErrorQuery, String streamUrl) {
		Boolean jobErrors = new Boolean(false);
		Integer errors = evaluationService.getTaskStreamCount(emailErrorQuery);
		if(errors > 0) jobErrors = Boolean.TRUE;
		
		// EvalStatusEmailImpl status
		String entryTag = "emailErrors";
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				entryTag, TaskStatusStandardValues.RUNNING, (new Boolean(
						jobErrors)).toString());
		return jobErrors;
	}

	private void addEmailText(List<String> ranText, String anc,
			String ancGroups, String ancUsers, String rem, String remGroups,
			String remUsers, String email, String taskStatusUrl, Boolean jobErrors, String since) {
		
		if(email != null) {
			ranText.add(email);
		}
		if(anc != null) {
			ranText.add(anc);
		}
		if(ancGroups != null) {
			ranText.add(ancGroups);
		}
		if(ancUsers != null) {
			ranText.add(ancUsers);
		}
		if(rem != null) {
			ranText.add(rem);
		}
		if(remGroups != null) {
			ranText.add(remGroups);
		}
		if(remUsers != null) {
			ranText.add(remUsers);
		}
		if(jobErrors != null && jobErrors) {
			// link to details
			ranText.add("The email job had problems. \nDetails are <a href='" + taskStatusUrl + "?streamTag=" 
					+ EMAIL_STREAM_TAG  + "&entryTag=" + ERROR + "&since=" + since + "'>here</a>.");
		}
		ranText.add("\n------------------------------------------------------------------------\n");
	}

	/**
	 * Check if the single-email announcements and reminders job ran.
	 * 
	 * @param baseEmailQuery
	 * 			the base query on the task status container.
	 * 			"?streamTag=" + EMAIL_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE;
	 * @param streamUrl
	 * @return true if the job ran and created a task stream, false otherwise
	 */
	private Boolean emailJobRan(String baseEmailQuery, String streamUrl) {
		Boolean jobRan = new Boolean(false);
		String query = baseEmailQuery + "&depth=0";
		Integer runs = evaluationService.getTaskStreamCount(query);
		if(log.isDebugEnabled()) log.debug(runs.toString() + " email runs using query " + query);
		if(runs > 0) jobRan = Boolean.TRUE;
		if(log.isDebugEnabled()) log.debug("'email job ran' is " + (new Boolean(jobRan)).toString());
		
		// EvalStatusEmailImpl status
		String entryTag = "emailRan";
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				entryTag, TaskStatusStandardValues.RUNNING, (new Boolean(
						jobRan)).toString());
		
		return jobRan;
	}
	
	/**
	 * Get the number of files that were imported.
	 * 
	 * @param baseImportQuery
	 * 		"?streamTag=" + IMPORT_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0";
	 * @param streamUrl
	 * @return
	 */
	private Integer importedTotal(String baseImportQuery, String streamUrl) {
		// CREATED
		String query = baseImportQuery + "&entryStatus=" + TaskStatusStandardValues.CREATED;
		Integer total = evaluationService.getTaskStreamCount(query);
		if(log.isDebugEnabled()) log.debug(total.toString() + " total files imported (using query " + query + ")");
		
		// EvalStatusEmailImpl status
		String entryTag = "filesTotal";
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				entryTag, TaskStatusStandardValues.RUNNING,
				total.toString());
		
		return total;
	}

	/**
	 * Get the number of files imported with errors.
	 * 
	 * @param baseQuery
	 * 			the base query on the task status container.
	 * 			"?streamTag=" + IMPORT_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0";
	 * @param streamUrl
	 * 			the Url of a task stream for EvalStatusEmailImpl.
	 * @return the number of files of this type
	 */
	private Integer importedWithError(String baseImportQuery, String streamUrl) {
		// FINISHED WITH_ERROR
		String query = baseImportQuery + "&entryStatus=" + TaskStatusStandardValues.FINISHED + "&entryTag=" + WITH_ERROR;
		Integer errors = evaluationService.getTaskStreamCount(query);
		if(log.isDebugEnabled()) log.debug(errors.toString() + " files imported with error (using query " + query + ")");
		
		// EvalStatusEmailImpl status
		String entryTag = "filesWithError";
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				entryTag, TaskStatusStandardValues.RUNNING,
				errors.toString());
		
		return errors;
	}

	/**
	 * Get the number of files imported without errors.
	 * 
	 * @param baseQuery
	 * 			the base query on the task status container.
	 * 			"?streamTag=" + IMPORT_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0";
	 * @param streamUrl
	 * 			the Url of a task stream for EvalStatusEmailImpl.
	 * @return the number of files of this type
	 */
	private Integer importedWithoutError(String baseImportQuery, String streamUrl) {
		// FINISHED WITHOUT_ERROR
		String query = baseImportQuery + "&entryStatus=" + TaskStatusStandardValues.FINISHED + "&entryTag=" + WITHOUT_ERROR;
		Integer good = evaluationService.getTaskStreamCount(query);
		if(log.isDebugEnabled()) log.debug(good.toString() + " files imported without error (using query " + query + ")");
		
		// EvalStatusEmailImpl status
		String entryTag = "filesWithoutError";
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				entryTag, TaskStatusStandardValues.RUNNING,
				good.toString());
		
		return good;
	}
	
	/**
	 * Get the number of files being imported that have not finished.
	 * 
	 * @param baseQuery
	 * 			the base query on the task status container.
	 * 			"?streamTag=" + IMPORT_STREAM_TAG + "&since=" + since + "&otype=" + CONTAINER_OTYPE + "&depth=0";
	 * @param streamUrl
	 * 			the Url of a task stream for EvalStatusEmailImpl.
	 * @return the number of files of this type
	 */
	private Integer importUnfinished(String baseImportQuery, String streamUrl) {
		// RUNNING
		String query = baseImportQuery + "&streamStatus=" + TaskStatusStandardValues.RUNNING;
		Integer stillRunning = evaluationService.getTaskStreamCount(query);
		if(log.isDebugEnabled()) log.debug(stillRunning.toString() + " files not yet finished using query " + query);
		
		// EvalStatusEmailImpl status
		String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
				"filesUnfinished", TaskStatusStandardValues.RUNNING,
				stillRunning.toString());
		
		return stillRunning;
	}

	/**
	 * Update the date/time to use as since parameter the next time this job is run.
	 * The start of the range of date/time values of entries is the since value, and
	 * the end of the range is the current date/time. The since value can be changed
	 * in the Control Email Settings UI.
	 * 
	 * @return the new Date
	 */
	private Date updateSince() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime( new Date(System.currentTimeMillis()) );
		updateConfig(EvalSettings.STATUS_SINCE_DATE, calendar.getTime());
		Date newDate = (Date) evalSettings.get(EvalSettings.STATUS_SINCE_DATE);
		if(log.isDebugEnabled()) log.debug(this + " STATUS_SINCE_DATE updated to " + newDate.toString());
		return newDate;
	}
	
	private void updateConfig(String key, Date date) {
		updateConfig(key, SettingsLogicUtils.getStringFromDate(date));
	}
	
	private void updateConfig(String key, int value) {
		updateConfig(key, Integer.toString(value));
	}
	
	private void updateConfig(String key, String value) {
		evalSettings.set(SettingsLogicUtils.getName(key),  value);
	}
}
