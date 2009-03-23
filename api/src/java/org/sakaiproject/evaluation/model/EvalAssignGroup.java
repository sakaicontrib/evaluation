
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * This is the assignment of a group to an evaluation and is how we know that the group should be
 * taking an eval<br/>
 * <b>NOTE:</b> the nodeId will be set to the id of the node which caused this group to be added if
 * it was added that way, it will be null if the group was assigned directly
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalAssignGroup extends EvalAssignHierarchy implements java.io.Serializable {

    public static final String SELECTION_OPTION_ALL = "all";
    public static final String SELECTION_OPTION_ONE = "one";
    public static final String SELECTION_OPTION_MULTIPLE = "multiple";

    public static final String SELECTION_TYPE_INSTRUCTOR = EvalConstants.ITEM_CATEGORY_INSTRUCTOR;
    public static final String SELECTION_TYPE_ASSISTANT = EvalConstants.ITEM_CATEGORY_ASSISTANT;
    public static final String SELECTION_TYPE_ENVIRONMENT = EvalConstants.ITEM_CATEGORY_ENVIRONMENT;

    // Fields

    private String eid;

    private String evalGroupId;

    private String evalGroupType;

    /**
     * An encoded string which indicates the stored selection settings for this assignment,
     * null indicates that there are no selection settings stored and to use {@link #SELECTION_OPTION_ALL} 
     * (the default which indicates no selections),
     * this is inherited from the evaluation it is associated with  <br/>
     * <b>WARNING:</b> getting and setting this value directly should not be done,
     * use the {@link #setSelectionOption(String, String)} and {@link #getSelectionOptions()} methods
     */
    protected String selectionSettings;

    // Constructors

    /** default constructor */
    public EvalAssignGroup() {
    }

    /**
     * BELOW minimal constructor, need to run this through a default setter method (setDefaults) to
     * set the Booleans before saving, setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah)
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            EvalEvaluation evaluation) {
        this(owner, evalGroupId, evalGroupType, evaluation, null, null, null, null, null);
    }

    /**
     * REQUIRED constructor
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            EvalEvaluation evaluation, Boolean instructorApproval,
            Boolean instructorsViewResults, Boolean studentsViewResults) {
        this(owner, evalGroupId, evalGroupType, evaluation, instructorApproval, instructorsViewResults, studentsViewResults, null, null);
    }

    /**
     * full constructor
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            EvalEvaluation evaluation, Boolean instructorApproval,
            Boolean instructorsViewResults, Boolean studentsViewResults, String nodeId,
            String selectionSettings) {
        this.lastModified = new Date();
        this.owner = owner;
        this.evalGroupId = evalGroupId;
        this.evalGroupType = evalGroupType;
        this.instructorApproval = instructorApproval;
        this.instructorsViewResults = instructorsViewResults;
        this.studentsViewResults = studentsViewResults;
        this.evaluation = evaluation;
        this.nodeId = nodeId;
        this.selectionSettings = selectionSettings;
    }

    /**
     * Validates that the selection option is valid and not null or empty
     * @param selectionOption must be one of the constants like {@link #SELECTION_OPTION_ALL} (the default)
     */
    public static void validateSelectionOption(String selectionOption) {
        if (selectionOption == null || "".equals(selectionOption)) {
            throw new IllegalArgumentException("Selection option must not be null or empty string");
        }
        if (! selectionOption.equals(SELECTION_OPTION_ALL) 
                && ! selectionOption.equals(SELECTION_OPTION_MULTIPLE) 
                && ! selectionOption.equals(SELECTION_OPTION_ONE)) {
            throw new IllegalArgumentException("Selection type must equal one of the SELECTION_OPTION constants in EvalAssignGroup");
        }
    }

    /**
     * Validates that the selection type is valid and not null or empty
     * @param selectionType must be one of the constants like {@link #SELECTION_TYPE_INSTRUCTOR}
     */
    public static void validateSelectionType(String selectionType) {
        if (selectionType == null || "".equals(selectionType)) {
            throw new IllegalArgumentException("Selection type must not be null or empty string");
        }
        if (! selectionType.equals(SELECTION_TYPE_INSTRUCTOR) 
                && ! selectionType.equals(SELECTION_TYPE_ASSISTANT) 
                && ! selectionType.equals(SELECTION_TYPE_ENVIRONMENT)) {
            throw new IllegalArgumentException("Selection type must equal one of the SELECTION_TYPE constants in EvalAssignGroup");
        }
    }


    /**
     * Get the selection settings out of this assign group (decoded),
     * this is a map of selection type constants like 
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * to selection option constant like {@link #SELECTION_OPTION_ONE}, 
     * this will be empty of no selection options are set for this assign group,
     * otherwise this will contain the selection options which are set only 
     * (not set should be assumed to mean the default: {@link EvalAssignGroup#SELECTION_OPTION_ALL}) <br/>
     * use the {@link EvalUtils#getSelectionSetting(String, EvalAssignGroup, EvalEvaluation)} method to make comparison easier and more standard
     * 
     * @return the selections as type constant => option constant (e.g. {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} => {@link EvalAssignGroup#SELECTION_OPTION_ONE}) if any are set
     */
    public Map<String, String> getSelectionOptions() {
        return decodeSelectionSettings(this.selectionSettings);
    }

    /**
     * Sets the selections to store for a specific category type:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * Can also clear the values for a type
     * 
     * @param selectionType the type constant to store selections for:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * @param selectionOption one of the constants like {@link #SELECTION_OPTION_ONE},
     * indicates the selection option to use for this selection type,
     * if set to null or {@link #SELECTION_OPTION_ALL} then this selection type will be removed from the stored options
     */
    public void setSelectionOption(String selectionType, String selectionOption) {
        Map<String, String> selections = getSelectionOptions();
        handleSelectionOption(selectionType, selectionOption, selections);
        setSelectionSettings( encodeSelectionSettings(selections) );
    }

    /**
     * Reduce code duplication
     * Sets the given selection type and option into the given map,
     * all params must not be null
     */
    public static void handleSelectionOption(String selectionType, String selectionOption,
            Map<String, String> selections) {
        EvalAssignGroup.validateSelectionType(selectionType);
        if (selectionOption == null 
                || SELECTION_OPTION_ALL.equals(selectionOption)) {
            selections.remove(selectionType);
        } else {
            EvalAssignGroup.validateSelectionOption(selectionOption);
            selections.put(selectionType, selectionOption);
        }
    }

    /**
     * Encodes a map into the selectionSettings coded string
     * @param selections the selections map (type -> option)
     * @return the selectionSettings code string OR null if the map is null or empty
     */
    public static String encodeSelectionSettings(Map<String, String> selections) {
        String encoded = null;
        if (selections != null && ! selections.isEmpty()) {
            // build encoded string {instructor:one}{assistant:multiple}
            StringBuilder sb = new StringBuilder();
            for (Entry<String, String> entry : selections.entrySet()) {
                sb.append('{');
                sb.append(entry.getKey());
                sb.append(':');
                sb.append(entry.getValue());
                sb.append('}');
            }
            encoded = sb.toString();
        }
        return encoded;
    }

    /**
     * Decodes a selectionSettings code string into a map of selections
     * @param encodedSelections the selectionSettings code string
     * @return a map of selection setting constants -> selection option constants (empty map if the input is null)
     */
    public static Map<String, String> decodeSelectionSettings(String encoded) {
        Map<String, String> selections;
        if (encoded != null) {
            encoded = encoded.trim();
        }
        if (encoded == null || "".equals(encoded)) {
            selections = new HashMap<String, String>(0);
        } else {
            selections = new HashMap<String, String>();
            try {
                // remove the outer brackets
                encoded = encoded.substring(1, encoded.lastIndexOf('}'));
                // split it
                String[] parts = encoded.split("\\}\\{");
                for (String part : parts) {
                    int pos = part.indexOf(':');
                    String key = part.substring(0, pos);
                    String value = part.substring(pos+1, part.length());
                    selections.put(key, value);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid encoded string supplied ("+encoded+"): must be generated using the encodeSelectionSettings method: " + e, e);
            }
        }
        return selections;
    }


    // GETTERS and SETTERS

    public String getEid() {
        return this.eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        this.evalGroupId = evalGroupId;
    }

    public String getEvalGroupType() {
        return evalGroupType;
    }

    public void setEvalGroupType(String evalGroupType) {
        this.evalGroupType = evalGroupType;
    }

    public String getSelectionSettings() {
        return selectionSettings;
    }

    public void setSelectionSettings(String selectionSettings) {
        selectionSettings = validateSelectionSettings(selectionSettings);
        this.selectionSettings = selectionSettings;
    }

    /**
     * Validates that a selection settings string is valid and cleans it up if needed
     * @param selectionSettings an encoded selection settings string
     * @return the valid selection settings string OR null (which is still valid)
     * @throws IllegalArgumentException is the string is invalid
     */
    public static String validateSelectionSettings(String selectionSettings) {
        if (selectionSettings != null) {
            selectionSettings = selectionSettings.trim();
        }
        if ("".equals(selectionSettings)) {
            selectionSettings = null;
        }
        if (selectionSettings != null) {
            // just try to decode it to make sure it is valid
        	decodeSelectionSettings(selectionSettings);
        }
        return selectionSettings;
    }

}
