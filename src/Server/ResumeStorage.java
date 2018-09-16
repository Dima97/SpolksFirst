package Server;

import java.util.HashMap;
import java.util.Map;

public class ResumeStorage {

    String currentUniqueID = "";

    Map<String, Map<String, Integer>> clientsResume = new HashMap<>();

    public void setClientsResumeID(String uniqueID){
        if(!clientsResume.containsKey(uniqueID)){
            clientsResume.put(uniqueID, null);
        }
        currentUniqueID = uniqueID;
    }

    public void deleteCurrentResume(){
        clientsResume.remove(currentUniqueID);
    }


    public void refreshResume(String fileName, int length){
        HashMap<String, Integer> downloadedFileInfo = new HashMap<>();
        downloadedFileInfo.put(fileName, length);
        clientsResume.put(currentUniqueID, downloadedFileInfo);
    }

    public Integer getProgress(String line){
        return (clientsResume.get(currentUniqueID) == null)? 0 : clientsResume.get(currentUniqueID).get(line);
    }

    public void removeCurrentFile(String fileName){
        clientsResume.get(currentUniqueID).remove(fileName);
    }
}
