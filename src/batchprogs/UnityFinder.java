package batchprogs;

@SuppressWarnings("serial")
public class UnityFinder extends URLsearch {
    public static final String UNITY_EXTENSION = ".*[.]unity3d";
    
    public UnityFinder() {
        super(UNITY_EXTENSION);
    }
}
