package vm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MailsIDToDisplay {
		
	public MailsIDToDisplay(Long id, String mailName, Date sendDate) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
	 		date =format.format(sendDate);
	 		emailId = id.toString();
	 		emailSubject = mailName;
	}
		
	public MailsIDToDisplay() {
	}

	public String date;
	public String emailId;
	public String emailSubject;
}