package callback;

import objects.FileObject;

import java.util.List;

public abstract class CompareCallback {

    public abstract void onComplete(List<FileObject> filesDownload, List<FileObject> filesUpload);

}
