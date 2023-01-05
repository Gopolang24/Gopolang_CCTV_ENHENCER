import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * @author G Mmutlwane 201901735
 *  
 * This class is responsible for GUI, 
 * it implements the log in scene and the image enhancer scene.
 */
public class clientPane extends GridPane {

	//Buttons
	private Button btnLogin;
	private Button btnDilation;
	private Button btnCanny;
	
	//text area.
	private TextArea txtArea;

	//image and imageview.
	private Image dil;
	private ImageView viewOrig;
	private ImageView viewDilation;
	private ImageView viewCanny;
	
	private String DilationURL = "/api/Dilation";
	private String CannyURL = "/api/Canny";
	
	
	private TextField username;
	private PasswordField password;
	private Label lblUserName;
	private Label lblUserPass;
	
	Socket s = null;
	BufferedReader br = null;
	InputStream is = null;
	DataOutputStream dos = null;
	OutputStream os = null;
	BufferedOutputStream bos = null;

	/**
	 * clientPane constructor class
	 * @param stage
	 *             represents a window that we place scene on.
	 */
	public clientPane(Stage stage) {
		setUpLoginComponents(); // function responsible for log in components
		btnLogin(stage); // log in button.
	}
	
	/**
	 * This function consist of buttons we need to enhance our images
	 */
	private void enhancer() {
		// Dilation button
		btnDilation.setOnAction(e -> {
			String encodedFile = null;
			try {
				// Gets the image from the Data folder
				File Imgfile = new File("data", "cctv.jpg");
				FileInputStream fis = new FileInputStream(Imgfile);
				byte[] b = new byte[(int) Imgfile.length()];
				fis.read(b);
				encodedFile = new String(Base64.getEncoder().encodeToString(b));
				byte[] bSend = encodedFile.getBytes();

				// sends and writes commands to the API
				dos.write(("POST " + DilationURL + " HTTP/1.1\r\n").getBytes());
				dos.write(("Content-Type: " + "application/text\r\n").getBytes());
				dos.write(("Content-Length: " + encodedFile.length() + "\r\n").getBytes());
				dos.write(("\r\n").getBytes());
				dos.write(bSend);
				dos.flush();
				dos.write(("\r\n").getBytes());

				txtArea.appendText("Sent POST Dilation Command\r\n");

				String response = "";
				String line = "";
				while (!(line = br.readLine()).equals("")) {
					response += line + "\n";
				}
				System.out.println(response);

				String ImgData = "";
				while ((line = br.readLine()) != null) {
					ImgData += line;
				}
				System.out.println(ImgData);

				String Base64Str = ImgData.substring(ImgData.indexOf('\'') + 1, ImgData.lastIndexOf('}') - 1);
				System.out.println(Base64Str);

				byte[] decodedstr = Base64.getDecoder().decode(Base64Str);

				// the new edited image is displayed
				dil = new Image(new ByteArrayInputStream(decodedstr));
				viewDilation.setImage(dil);
				txtArea.appendText("Image Successfully Dilated");
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		//Canny button
		btnCanny.setOnAction(e -> {
			try {
				// Reconnects the server
				s = new Socket("localhost", 5000);
				os = s.getOutputStream();
				is = s.getInputStream();
				br = new BufferedReader(new InputStreamReader(is));
				bos = new BufferedOutputStream(os);
				dos = new DataOutputStream(bos);

				txtArea.appendText("Socket Reconnected!!! \r\n");

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// gets the dilated image
			BufferedImage bf = SwingFXUtils.fromFXImage(dil, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			String encodedFile = null;
			try {
				// converts the dilated image into an array of bytes.
				ImageIO.write(bf, "jpg", baos);
				byte[] b = baos.toByteArray();
				encodedFile = new String(Base64.getEncoder().encodeToString(b));
				byte[] bSend = encodedFile.getBytes();

				// sends and writes commands to the API
				dos.write(("POST " + CannyURL + " HTTP/1.1\r\n").getBytes());
				dos.write(("Content-Type: " + "application/text\r\n").getBytes());
				dos.write(("Content-Length: " + encodedFile.length() + "\r\n").getBytes());
				dos.write(("\r\n").getBytes());
				dos.write(bSend);
				dos.flush();
				dos.write(("\r\n").getBytes());

				txtArea.appendText("Sent POST Canny Command\r\n");

				String response = "";
				String line = "";
				while (!(line = br.readLine()).equals("")) {
					response += line + "\n";
				}
				System.out.println(response);

				String ImgData = "";
				while ((line = br.readLine()) != null) {
					ImgData += line;
				}
				System.out.println(ImgData);

				String Base64St = ImgData.substring(ImgData.indexOf('\'') + 1, ImgData.lastIndexOf('}') - 1);
				System.out.println(Base64St);

				byte[] decodedstr = Base64.getDecoder().decode(Base64St);

				// the image is displayed
				Image can = new Image(new ByteArrayInputStream(decodedstr));
				viewCanny.setImage(can);

				txtArea.appendText("Image Successful Enhenced \r\n");

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		});		
	}

	/**
	 * log in button.
	 * button will also connect to the server.
	 * @param stage
	 */
	private void btnLogin(Stage stage) {
btnLogin.setOnAction(event -> {
			
			Alert alert = new Alert(AlertType.ERROR);
			if (username.getText().toString().equals("user") && password.getText().toString().equals("123")) {
				try {
					s = new Socket("localhost", 5000);
					os = s.getOutputStream();
					is = s.getInputStream();
					br = new BufferedReader(new InputStreamReader(is));
					bos = new BufferedOutputStream(os);
					dos = new DataOutputStream(bos);

				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//System.out.println("conected to port" + 5000);
				inhencerScene(stage);
			} else if (username.getText().isEmpty() && password.getText().isEmpty()) {
				alert.setHeaderText("PLEASE ENTER USERNAME AND PASSWORD!!!");
				alert.showAndWait();
			} else {
				alert.setHeaderText("Wrong username or password!!!");
				alert.showAndWait();
			}
		});
	}

	/**
	 * All the components needed to set up the log in scene.
	 */
	private void setUpLoginComponents() {
		setHgap(10);
		setVgap(10);
		setAlignment(Pos.CENTER);

		btnLogin = new Button("Login");
		lblUserName = new Label("User Name:");
		lblUserPass = new Label("User Password:");

		username = new TextField();
		password = new PasswordField();

		/// Add to the pane
		add(btnLogin, 0, 0);
		add(lblUserName, 0, 1);
		add(username, 1, 1);
		add(lblUserPass, 0, 2);
		add(password, 1, 2);

	}
	
	/**
	 * set up the enhancer scene
	 * @param stage
	 */
	public void inhencerScene(Stage stage) {
		GridPane inhencer = new GridPane();
		
		inhencer.setHgap(13);
		inhencer.setVgap(13);
		setAlignment(Pos.CENTER);
		
		viewOrig = new ImageView();
		btnDilation = new Button("DILATION");
		btnDilation.setPrefSize(95, 5);
		btnCanny = new Button("CANNY EDGES");
		txtArea = new TextArea();
		viewDilation = new ImageView();
		viewCanny = new ImageView();
		Image imgOrgs = new Image("file:data/cctv.jpg");
		viewOrig.setFitHeight(100);
		viewOrig.setFitWidth(250);
		viewOrig.setImage(imgOrgs);
		viewDilation.setFitHeight(100);
		viewDilation.setFitWidth(250);
		viewCanny.setFitHeight(100);
		viewCanny.setFitWidth(250);
		inhencer.add(viewOrig, 4, 2,5 ,4);
		inhencer.add(btnDilation, 1, 2);
		inhencer.add(btnCanny, 1, 3);
		inhencer.add(viewDilation, 1, 6,8,9);
		inhencer.add(viewCanny, 10, 7, 8, 7);
		inhencer.add(txtArea, 1, 15, 7, 5);
		
		enhancer();
		
		Scene scene = new Scene(inhencer);
		stage.setScene(scene);
		stage.setHeight(550);
		stage.setWidth(650);
		stage.show();
	}

}