package ec.com.dev.service.firebase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import ec.com.dev.model.FileUpload;

@Component
public class FirebaseStrategyImpl implements FirebaseStrategy {
	
	private StorageOptions options;
	
	@Value("${firebase.storage.bucket}")
	private String bucket;
	
	@Value("${firebase.storage.projectId}")
	private String projectId;
	
	Logger log = LoggerFactory.getLogger(FirebaseStrategyImpl.class);
	
	@PostConstruct
	private void initializeFirebase() throws Exception {
		FileInputStream serviceAccount = new FileInputStream(System.getProperty("user.dir").concat("/istb-storage-firebase.json"));
				
		this.options = StorageOptions.newBuilder().setProjectId(projectId)
				.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
	}
  
	@Override
	public FileUpload uploadFile(MultipartFile multipartFile) throws Exception {
		File file = convertMultiPartToFile(multipartFile);
    Path filePath = file.toPath();
    String objectName = generateFileName(multipartFile);

    Storage storage = this.options.getService();
    BlobId blobId = BlobId.of(bucket, objectName);
    
    String tokenRandomFirebase = UUID.randomUUID().toString();
    
    Map<String, String> firebaseStorageDownloadTokens = new HashMap<String, String>();
    firebaseStorageDownloadTokens.put("firebaseStorageDownloadTokens", tokenRandomFirebase);
    
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
    		.setContentType(multipartFile.getContentType())
    		.setMetadata(firebaseStorageDownloadTokens)
    		.build();
        
    Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
    
    log.info("Resp: " + blob);
    
    return new FileUpload(blob.getBucket(), 
				blob.getName(), 
				blob.getGeneration(), 
				blob.getSize(), 
				blob.getContentType(),
				this.getURL(blob.getName(), tokenRandomFirebase));
	}
	
	@Override
	public List<FileUpload> uploadFiles(MultipartFile[] files) throws Exception {
		List<FileUpload> filesUploads = new ArrayList<>();
		
		Arrays.asList(files).forEach(file -> {
			try {
				filesUploads.add(this.uploadFile(file));
			} catch (Exception e) {
				log.error("Error: " + e.getMessage());
			}
		});
		
		return filesUploads;
	}
	
	@Override
	public boolean deleteFile(String name) {
		Storage storage = this.options.getService();
		
		BlobId blobId = BlobId.of(bucket, name);
    
		boolean deleted = storage.delete(blobId);
		
		return deleted;
	}
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
		FileOutputStream fos = new FileOutputStream(convertedFile);
		fos.write(file.getBytes());
		fos.close();
		return convertedFile;
	}

	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
	}
	
	private String getURL (String name, String tokenRandomFirebase) {
		return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + name + "?alt=media&token=" + tokenRandomFirebase;
	}

}
