package com.utils;

import android.app.*;
import android.content.*;
import android.widget.*;
import java.io.*;
import java.math.*;
import java.security.*;

public class Common extends Activity
{
	//Toast提示
	public void tw(String str, Context context)
	{
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	
	
	
	/**
	 * 获取单个文件的MD5值！

	 * @param file
	 * @return
	 */

	public static String getFileMD5(String path)
	{
		File file = new File(path);
		if (!file.isFile())
		{
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try
		{
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1)
			{
				digest.update(buffer, 0, len);
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	



}
