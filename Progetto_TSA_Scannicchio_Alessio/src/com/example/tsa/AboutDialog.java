/*
	Copyright 2013 Alessio Scannicchio (alessio.scannicchio@gmail.com). 

 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at

  	http://www.apache.org/licenses/LICENSE-2.0

 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
*/

package com.example.tsa;

import android.app.Dialog; 
import android.content.Context;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog implements OnClickListener {

	// About message.
	private static final String HTML_ABOUT =
			"<b>TSA</b><br />"
			+ "Copyright 2013 <br />"
			+ "Alessio Scannicchio(alessio.scannicchio@gmail.com).<br />"
			+ "Licensed under the Apache License, Version 2.0 <br />"
			+ "<br />"
			+ "Credits: <br />"			
			+ "Application icon source: <br />"
			+ "Alexiscanny Android. Available at http://www.sanbros.it/ <br />"
			+ "<br />"
			+ "History: <br />"
			+ "1.0	Initial release <br />";

	public AboutDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// This window does not need title.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Use about_dialog layout for the content of this window.
		this.setContentView(R.layout.about_dialog);
		
		TextView aboutText = (TextView) this.findViewById(R.id.About_Dialog_Text);
  	aboutText.setText(android.text.Html.fromHtml(HTML_ABOUT));
  	
  	// Create OK button.
  	Button aboutCloseBtn = (Button) this.findViewById(R.id.About_Dialog_CloseButton);
  	aboutCloseBtn.setOnClickListener(this);
	}

	public void onClick(View v) {
		// Close button
		this.dismiss();
	}
}