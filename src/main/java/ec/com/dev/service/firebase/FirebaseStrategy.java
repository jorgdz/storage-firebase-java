package ec.com.dev.service.firebase;

import org.springframework.web.multipart.MultipartFile;

import ec.com.dev.model.FileUpload;

public interface FirebaseStrategy {
	 
	FileUpload uploadFile(MultipartFile multipartFile) throws Exception;
	
	boolean deleteFile (String name);
}
