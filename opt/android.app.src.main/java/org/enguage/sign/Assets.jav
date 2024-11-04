package org.enguage.sign;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.strings.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Assets {
	private Assets() {}
	
	public  static final String NAME = "assets";
	public  static final int      ID = 20901783; //Strings.hash( NAME );
	
	private static Activity  context = null; // if null, not on Android
	public  static Activity  context() {return context;}
	public  static void      context( Activity activity ) {context = activity;}
	
	public static InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = context().getAssets().open( name );
		} catch (IOException ignore) {
			//audit.error( "Assets.getStream(): gone missing: "+ name );
		}
		return is;
	}
	public static String path() {return context.getExternalFilesDir(".").getPath();}
	public static String[] list( String path ) {
		if (path.endsWith("/.")) path = path.substring(0, path.length()-2);
		String[] names = null;
		try {
			names = context().getAssets().list( path );
		} catch (IOException iox) {}
		return names;
	}
	/*
	 * this expeirmental code operated the torch...
	 */
	public static Strings perform( Strings cmds ) {
		Strings rc = Response.notOkay();
		if (!cmds.isEmpty()) {

			String cmd = cmds.remove( 0 );
			if (!cmds.isEmpty() && (cmd.equals("torch") || cmd.equals("light"))) {

				CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
				int defaultLevel = 0, maximumLevel = 0;
				String[] ids = {};

				try {
					ids = cm.getCameraIdList();
					if (ids.length <= 0)
						rc = new Strings( Response.notOkay() +", there are no lights on this device" );
					else {
						/*
						 * This all works on id[ 0 ] for the moment...
						 */
						CameraCharacteristics cc = cm.getCameraCharacteristics(ids[ 0 ]);
						defaultLevel = cc.get( CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL );
						maximumLevel = cc.get( CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL );

						cmd = cmds.remove( 0 );
						if (cmd.equals( "on" )) {
							cm.setTorchMode( ids[ 0 ], true);
							if (maximumLevel > 1) // doesn't like default == level of 1
								cm.turnOnTorchWithStrengthLevel( ids[ 0 ], defaultLevel );
							rc = Response.okay( "torch is now on" );

						} else if (cmd.equals( "off" )) {
							cm.setTorchMode(ids[ 0 ], false);
							rc = Response.okay("torch is now off " );

						} else if (cmd.equals( "get" )) {
							String value = cmds.isEmpty() ? "maximum" : cmds.remove( 0 );
							if (value.equals( "maximum" ))
								rc = Response.okay("maximum is "+ maximumLevel );
							else if( value.equals( "default" ))
								rc = Response.okay("default is "+ defaultLevel );
							else
								rc = Response.okay("level is "+ cm.getTorchStrengthLevel( ids[ 0 ]));

						} else if (cmd.equals( "set" )) {
							// get and normalise level...
							int max = cc.get( CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL );
							int level = cmds.isEmpty() ? 0 :
									Integer.parseInt( cmds.remove( 0 ));
							if (level > max)
								level = max;
							else if (level < 0)
								level = 0;

							cm.turnOnTorchWithStrengthLevel( ids[ 0 ], level );
							rc = Response.okay("torch is now set to "+ level );
						}
					}

				} catch (IllegalArgumentException x) {
					rc = Response.notOkay("an illegal argument exception occurred in turning light "+ cmd +", default level "+ defaultLevel +", "
							+ ids.length +"id are "+ new Strings(ids).toString());

				} catch (CameraAccessException x) {
					rc = Response.notOkay("a camera access exception occurred in turning light "+ cmd );

				} catch (Exception x) {
					rc = Response.notOkay("an unknown exception occurred in turning light "+ cmd );

				}
			} else {
				rc = Response.notOkay("device is not 'torch' or 'light'" );
			}
		}
		return rc;
}	}
