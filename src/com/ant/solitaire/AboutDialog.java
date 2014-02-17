package com.ant.solitaire;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);
		this.setTitle(R.string.title_about);
		Button but = (Button) this.findViewById(R.id.btnAbout);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
