// IRemoteFileUploadService.aidl
package in.ac.srmuniv.srm_uploadfileremoteservice;



/* The name of the remote service */
interface IRemoteFileUploadService {

   /* A simple Method which will return a message string */
     String uploadFile(String sourceFileUri);
}