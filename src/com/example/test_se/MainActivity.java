package com.example.test_se;

import iaik.utils.Base64Exception;
import iaik.utils.Util;
import iaik.x509.X509Certificate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import at.iaik.skytrust.element.receiver.skytrustprovider.GenericKeystore;
import at.iaik.skytrust.element.receiver.skytrustprovider.SkytrustKey;
import at.iaik.skytrust.element.receiver.skytrustprovider.SkytrustProvider;
import at.tugraz.iaik.skytrust.sampleapp.SkyTrustAPI;




public class MainActivity extends Activity {

	private TextView tf_cipher, tf_packet,tf_plaintext,tf_decrypted;
	private Spinner spinner;
	
	X509Certificate _cert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tf_cipher = (TextView) findViewById(R.id.tf_cipher);
		tf_packet = (TextView) findViewById(R.id.tf_packet);
		tf_plaintext = (TextView) findViewById(R.id.tf_plaintext);
		tf_decrypted = (TextView) findViewById(R.id.tf_decryptedtext);
		spinner = (Spinner) findViewById(R.id.spinner1);

		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);
		Security.addProvider(new SkytrustProvider(this));
		


	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void ClickedExportCert(View view){
		
	    if(_cert == null)
	    	return;
		// Get the directory for the user's public download directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS), spinner.getSelectedItem().toString()+".cert");

	    try {
			BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(file));
			outstream.write(_cert.getEncoded());
			outstream.flush();
			outstream.close();
			Toast.makeText(getApplicationContext(), "Certificate exported to Downloads/"+spinner.getSelectedItem().toString()+".cert", Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ClickedEncrypt(View view){
		//SkyTrustAPI skytrust = new SkyTrustAPI();
		
		new asyncEncrypt().execute(tf_plaintext.getText().toString());

	}

	public void ClickedDecrypt(View view){

		new asyncDecrypt().execute(tf_cipher.getText().toString());

	}

	public void ClickedGetCert(View view){
		new asyncGetCert().execute(spinner.getSelectedItem().toString());
	}
	
	public void ClickedEncryptJce(View view){
		new asyncEncryptJCE().execute(tf_plaintext.getText().toString());
	}
	
	private class asyncDecrypt extends AsyncTask<String, Void, byte[]> {
		protected byte[] doInBackground(String... params) {
			byte[] res = null;
			try {
				String user =spinner.getSelectedItem().toString();
				Cipher c = Cipher.getInstance("AES","SkytrustPhone");
				//SkytrustKey k = new SkytrustKey(null, "yubikey");
				SkytrustKey k = new SkytrustKey(null, user);

				c.init(Cipher.DECRYPT_MODE, k);
				res = (c.doFinal(Util.Base64Decode(params[0].getBytes())));

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Base64Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return res;

		}

		protected void onPostExecute(byte[] result) {
			//showDialog("Downloaded " + result + " bytes");
			if(result != null)
				tf_decrypted.setText(new String(result));
			else
				tf_decrypted.setText("error occured");

		}


	}


	private class asyncEncrypt extends AsyncTask<String, Void, byte[]> {
		protected byte[] doInBackground(String... params) {
			
			byte[] res = null;
			try {
				Cipher c = Cipher.getInstance("AES","SkytrustPhone");
				String user =spinner.getSelectedItem().toString();
				//SkytrustKey k = new SkytrustKey(null, "yubikey");
				SkytrustKey k = new SkytrustKey(null, user);
				c.init(Cipher.ENCRYPT_MODE, k);
				res = c.doFinal(params[0].getBytes());
				if(res != null)
					res = Util.Base64Encode(res);

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return res;

		}

		protected void onPostExecute(byte[] result) {
			//showDialog("Downloaded " + result + " bytes");
			if(result != null)
				tf_cipher.setText(new String(result));
			else
				tf_cipher.setText("error occured");

		}


	}
	
	private class asyncEncryptJCE extends AsyncTask<String, Void, byte[]> {
		protected byte[] doInBackground(String... params) {
			
			if(_cert == null)
				return null;
			
			byte[] res = null;
			try {
				Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "IAIK");
				c.init(Cipher.ENCRYPT_MODE, _cert.getPublicKey());
				res = c.doFinal(params[0].getBytes());


			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return res;

		}

		protected void onPostExecute(byte[] result) {
			//showDialog("Downloaded " + result + " bytes");
			if(result != null)
				tf_cipher.setText(new String(Util.Base64Encode(result)));
			else
				tf_cipher.setText("error occured");

		}


	}

	private class asyncGetCert extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... params) {


			GenericKeystore ks = new GenericKeystore();
			String user =spinner.getSelectedItem().toString();
			//X509Certificate cert = (X509Certificate) ks.engineGetCertificate("yubikey");
			X509Certificate cert = (X509Certificate) ks.engineGetCertificate(user);
			
			if(cert != null){
				_cert = cert;
				Log.d("skytrust", cert.toString());
				return cert.toString();
			}
			return "error occured";


		}

		protected void onPostExecute(String result) {
			//showDialog("Downloaded " + result + " bytes");
			tf_packet.setText(result);

		}
	}


}
