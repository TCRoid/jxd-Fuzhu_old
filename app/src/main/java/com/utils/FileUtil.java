package com.utils;

import java.io.File;

public class FileUtil
{

	//删除文件夹
	public static boolean deleteFolder(String folderPath)
	{
			deleteAllFile(folderPath); //删除里面所有文件（夹）
			File file = new File(folderPath);
			return file.delete();//删除空文件夹，同时返回布尔值
	}

	
	//删除指定文件夹下的所有文件
	public static boolean deleteAllFile(String path)
	{
		boolean flag = false;
		File file = new File(path);
		if (!file.exists())
		{
			return flag;
		}
		if (!file.isDirectory())
		{
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++)
		{
			if (path.endsWith(File.separator))
			{
				temp = new File(path + tempList[i]);
			}
			else
			{
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile())
			{
				temp.delete();
			}
			if (temp.isDirectory())
			{
				deleteAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
				deleteFolder(path + "/" + tempList[i]);//再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
	

}
