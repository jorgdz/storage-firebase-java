package ec.com.dev.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ec.com.dev.model.FileUpload;
import ec.com.dev.service.firebase.FirebaseStrategy;

@Controller
public class HomeController {

	@Autowired
	private FirebaseStrategy firebaseStrategy;

	Logger log = LoggerFactory.getLogger(HomeController.class);

	@GetMapping({ "/", "" })
	public String index(Model model) {

		model.addAttribute("titulo", "Thymeleaf con Dropzone");
		return "index";
	}

	@PostMapping(value = "/upload", produces = "application/json")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {

		FileUpload uploaded = firebaseStrategy.uploadFile(file);

		return ResponseEntity.ok(uploaded);
	}
	
	@PostMapping(value = "/upload-files", produces = "application/json")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile[] file) throws Exception {
		if(file.length < 0) {
			return ResponseEntity
					.status(400)
					.body("{\"error\":\"Debe seleccionar una imagen\"}");
		}
		
		List<FileUpload> uploaded = firebaseStrategy.uploadFiles(file);

		return ResponseEntity.ok(uploaded);
	}
	
	@DeleteMapping(value="/delete-file", produces="application/json")
	public ResponseEntity<?> delete(@RequestParam("name") String name) throws Exception {

		if (this.firebaseStrategy.deleteFile(name)) {
			return ResponseEntity.ok("{\"response\":\"Imagen eliminada\"}");
		} else {
			return ResponseEntity
					.status(400)
					.body("{\"error\":\"Ha ocurrido un error\"}");
		}
	}
}
