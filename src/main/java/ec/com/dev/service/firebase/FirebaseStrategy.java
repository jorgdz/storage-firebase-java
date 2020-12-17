package ec.com.dev.service.firebase;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ec.com.dev.model.FileUpload;

public interface FirebaseStrategy {
	 
	FileUpload uploadFile(MultipartFile multipartFile) throws Exception;
	
	List<FileUpload> uploadFiles (MultipartFile[] file) throws Exception;
	
	boolean deleteFile (String name);
}
