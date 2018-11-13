package util;

import java.util.HashMap;

public class PacketsScope {
    private HashMap<Integer, byte[]> scope = new HashMap<>();

    public boolean setValue (int key, byte[] bytes){
        if(!isFull()) {
            scope.put(key, bytes);
            return true;
        }
        return false;
    }

    public void clearScope(){
        scope.clear();
    }

    public int getCountNullPackets(){
        int count = 0;
        for(int i = 0; i < scope.size(); i++){
            if(scope.get(i) == null){
                count++;
            }
        }
        return count;
    }

    public int getFirstNullPosition(){
        for(int i = 0; i < 5; i++) {
            if (scope.get(i) == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFull(){
        return scope.size() > 4;
    }


    public HashMap<Integer, byte[]> getScope() {
        return scope;
    }

    public void setScope(HashMap<Integer, byte[]> scope) {
        this.scope = scope;
    }
}
