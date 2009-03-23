
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This is an evaluation response and reprsents a single user's response to a single evaluation,
 * answers are stored in the answers set,
 * to complete the response, set the end time (otherwise it is saved as a partial response) and save it
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalResponse implements java.io.Serializable {

    // Fields

    private Long id;

    private Date lastModified;

    private String owner;

    private String evalGroupId;

    private Date startTime;

    private Date endTime;

    private Set<EvalAnswer> answers = new HashSet<EvalAnswer>(0);

    private EvalEvaluation evaluation;

    /**
     * This holds a comment for the users response to this eval,
     * i.e. this would be the comment for the evaluation itself from this user
     */
    private String comment;

    /**
     * This stores all selections (instructor and assistant and otherwise) that this user made
     * in an encoded string,
     * if this is null then it represents that the selections were set to {@link EvalAssignHierarchy#SELECTION_OPTION_ALL}
     */
    private String selectionsCode;

    // NON_PERSISTENT fields

    /**
     * This is a special variable we are using to keep track of whether this response was
     * complete when it was loaded from the database, if it is true then it is/was complete,
     * if it is false then it was not complete<br/>
     * <b>WARNING:</b> Do NOT modify this value yourself, it should only be changed internally in the DAO
     */
    public boolean complete = false;

    // Constructors

    /** default constructor */
    public EvalResponse() {
    }

    /** minimal constructor */
    public EvalResponse(String owner, String evalGroupId, EvalEvaluation evaluation, Date startTime) {
        this(owner, evalGroupId, evaluation, startTime, null, null);
    }

    /** full constructor */
    public EvalResponse(String owner, String evalGroupId, EvalEvaluation evaluation, Date startTime, Date endTime,
            Set<EvalAnswer> answers) {
        this.lastModified = new Date();
        this.owner = owner;
        this.evalGroupId = evalGroupId;
        this.startTime = startTime;
        this.endTime = endTime;
        if (answers != null) {
            this.answers = answers;
        }
        this.evaluation = evaluation;
    }

    /**
     * Get the selections out of this response (decoded),
     * this is a map of selection type constants like 
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * to ids (typically userIds)
     * 
     * @return the selections as type constant => selection ids (e.g. {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} => instructorIds)
     */
    public Map<String, String[]> getSelections() {
        return decodeSelections(this.selectionsCode);
    }

    /**
     * Sets the selections to store for a specific selection type:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * Can also clear the values for a type
     * 
     * @param selectionType the type constant to store selections for:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * @param selectionIds the ids for the type of selection (userIds),
     * if this is null then the selections for that type are removed
     */
    public void setSelections(String selectionType, String[] selectionIds) {
        EvalAssignGroup.validateSelectionType(selectionType);
        Map<String, String[]> selections = getSelections();
        if (selectionIds == null || selectionIds.length == 0) {
            selections.remove(selectionType);
        } else {
            selections.put(selectionType, selectionIds);
        }
        setSelectionsCode( encodeSelections(selections) );
    }

    /**
     * Encodes a map into the selectionsCode string
     * @param selections the selections map
     * @return the selection code string OR null if the map is null or empty
     */
    public static String encodeSelections(Map<String, String[]> selections) {
        String encoded = null;
        if (selections != null && ! selections.isEmpty()) {
            // build encoded string {A[1,2,3]}{B[1,2]}
            StringBuilder sb = new StringBuilder();
            for (Entry<String, String[]> entry : selections.entrySet()) {
                sb.append('{');
                sb.append(entry.getKey());
                sb.append('[');
                String[] ids = entry.getValue();
                for (int i = 0; i < ids.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(ids[i]);
                }
                sb.append("]}");
            }
            encoded = sb.toString();
        }
        return encoded;
    }

    /**
     * Decodes a selections code string into a map of selections
     * @param encodedSelections the selections code string
     * @return a map of selection item category constants -> selection ids (empty map if the input is null)
     */
    public static Map<String, String[]> decodeSelections(String encoded) {
        Map<String, String[]> selections;
        if (encoded != null) {
            encoded = encoded.trim();
        }
        if (encoded == null || "".equals(encoded)) {
            selections = new HashMap<String, String[]>(0);
        } else {
            selections = new HashMap<String, String[]>();
            try {
                // remove the outer brackets
                encoded = encoded.substring(1, encoded.lastIndexOf('}'));
                // split it
                String[] parts = encoded.split("\\}\\{");
                for (String part : parts) {
                    int pos = part.indexOf('[');
                    String key = part.substring(0, pos);
                    String arrayStr = part.substring(pos+1, part.lastIndexOf(']'));
                    String[] value = arrayStr.split(",");
                    selections.put(key, value);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid encoded string supplied ("+encoded+"): must be generated using the encodeSelections method: " + e, e);
            }
        }
        return selections;
    }

    // getters and setters

    public Set<EvalAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<EvalAnswer> answers) {
        this.answers = answers;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        this.evalGroupId = evalGroupId;
    }

    public EvalEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(EvalEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSelectionsCode() {
        return selectionsCode;
    }

    public void setSelectionsCode(String selectionsCode) {
        selectionsCode = validateSelectionsCode(selectionsCode);
        this.selectionsCode = selectionsCode;
    }

    /**
     * Validates that a selections code string is valid and cleans it up if needed
     * @param selectionsCode an encoded selections string
     * @return the valid selections code string OR null (which is still valid)
     * @throws IllegalArgumentException is the string is invalid
     */
    public static String validateSelectionsCode(String selectionsCode) {
        if (selectionsCode != null) {
            selectionsCode = selectionsCode.trim();
        }
        if ("".equals(selectionsCode)) {
            selectionsCode = null;
        }
        if (selectionsCode != null) {
            // just try to decode it to make sure it is valid
            decodeSelections(selectionsCode);
        }
        return selectionsCode;
    }

}
