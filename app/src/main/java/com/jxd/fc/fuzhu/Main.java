package com.jxd.fc.fuzhu;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.utils.*;
import java.io.*;
import java.util.*;

import android.support.v7.app.AlertDialog;
import android.view.View.OnClickListener;
import android.support.transition.*;

public class Main extends AppCompatActivity 
{
	private String fileRoot;
	private String Path;
	private String DataName;
	private ListView ListV;
	private static LinearLayout LL1;
	private static LinearLayout LL2;
	private static LinearLayout LL3;
	private List<Map<String,Object>> fileList = new ArrayList<Map<String,Object>>();
	private Common Com;
	private FileUtil FileUtil;
	private SharedPreferences SPs;
	private String apkPath;

	private ProgressDialog ProDialog;
	private Runnable runnable;
	private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Com = new Common();
		FileUtil = new FileUtil();
		handler = new Handler();
		DataName = "com.jxd.fc";
		apkPath = getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/jxd.apk";//cache目录下的apk安装文件

		LL1 = (LinearLayout) findViewById(R.id.LinearLayout1);//列表
		LL2 = (LinearLayout) findViewById(R.id.LinearLayout2);//无游戏数据
		LL3 = (LinearLayout) findViewById(R.id.LinearLayout3);//空文件夹
		ListV = (ListView) findViewById(R.id.ListV);

		//SharedPreferences初始化--->软件包名
		SPs = this.getSharedPreferences("setting", Context.MODE_WORLD_READABLE);
		if (SPs.getString("pkgName", "").equals(""))
		{
			SPs.edit().putString("pkgName", DataName).commit();
		}


		//检测是否有游戏数据
		fileRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + DataName;
		Path = fileRoot; //当前目录(实时)

		File files = new File(fileRoot);
		if (!files.exists() || !files.isDirectory() || files.list().length == 0)//判断文件夹是否存在
		{
			LL2.setVisibility(View.VISIBLE);
			LL1.setVisibility(View.GONE);
		}
		else
		{
			LL1.setVisibility(View.VISIBLE);
			LL2.setVisibility(View.GONE);

			openDir(Path);
		}//else




		//设置列表点击事件
		ListV.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					if (Integer.parseInt(fileList.get(p3).get("isDirectory") + "") == 2) //是文件夹
					{
						Path = Path + File.separator + fileList.get(p3).get("fileName") + "";//路径
						openDir(Path);
					}
					else //文件
					{
						String filePath = Path + File.separator + fileList.get(p3).get("fileName") + "";//文件路径
						String fileName = fileList.get(p3).get("fileName") + "";//文件名
						EditDialog(filePath, fileName);
					}


				}

			});




		//列表长按事件
		ListV.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					String filePath = Path + File.separator + fileList.get(p3).get("fileName") + "";//文件路径
					String fileName = fileList.get(p3).get("fileName") + "";//文件名
					SelDialog(filePath, fileName);

					return true; //返回true，就不执行列表点击事件
				}

			});





		//新建 悬浮按钮
		FloatingActionButton FloatB_new = (FloatingActionButton) findViewById(R.id.FloatBtn_new);
        FloatB_new.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view)
				{
					File files = new File(fileRoot);
					if (!files.exists() || !files.isDirectory() || files.list().length == 0)//判断文件夹是否存在
					{
						Com.tw("无游戏数据", getApplicationContext());
					}
					else
					{
						NewDialog(Path);
					}
				}
			});


		//启动游戏 悬浮按钮
		FloatingActionButton FloatB_play = (FloatingActionButton)findViewById(R.id.FloatBtn_play);
		FloatB_play.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					openApp(SPs.getString("pkgName", ""));
				}
			});



		//安装软件
		TextView text_install = (TextView)findViewById(R.id.TextIn);
		text_install.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					install_jxd();//安装九仙道
				}
			});



    }//onCreate






	public void openDir(String Dir) //打开文件夹
	{
		ListV = (ListView) findViewById(R.id.ListV);

		fileList = GetPathFilsList(Dir);

		FileBrowserAdapter phoneFileBrowserAdapter = new FileBrowserAdapter(getApplicationContext(), fileList);
		ListV.setAdapter(phoneFileBrowserAdapter);
	}



	public static List<Map<String,Object>> GetPathFilsList(String path)
	{
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> filelist = new ArrayList<Map<String,Object>>();

		String[] Files = new File(path).list();
		if (Files.length == 0)//空文件夹
		{
			LL1.setVisibility(View.GONE);
			LL3.setVisibility(View.VISIBLE);
			return null;
		}
		else
		{
			LL3.setVisibility(View.GONE);
			LL1.setVisibility(View.VISIBLE);
			
			
			try
			{
				for (String file : Files)
				{
					Map<String, Object> map = new HashMap<String, Object>();
					if (new File(path + File.separator + file).isDirectory())
					{
						map.put("isDirectory", 2);
						map.put("fileName", file);

						list.add(map);
					}
					else
					{
						map.put("isDirectory", 1);
						map.put("fileName", file);

						filelist.add(map);
					}


				}
				list.addAll(filelist);
				return list;
			}
			catch (Exception e)
			{
				return null;
			}
		}//else
	}





	public void SelDialog(final String path, final String name) //选择项 弹窗
	{
		AlertDialog.Builder build = new AlertDialog.Builder(Main.this);
		build.setTitle("选择操作");

		final String[] SelList = {"重命名","删除"};
		build.setItems(SelList, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					//下标是从0开始的
					switch (SelList[which])
					{
						case "重命名":
							renameFile(path, name);
							break;

						case "删除":
							deleteFile(path, name);
							break;
					}
				}
			});

		build.setPositiveButton("取消", null);
		build.create().show();
	}








	//安装九仙道
	public void install_jxd()
	{
		if (new File(apkPath).exists() && Com.getFileMD5(apkPath).equals("3ec5471f30037db9d3b193e9cdeb4f35"))
		{
			//本地已有apk文件
			installApk(apkPath);//本地安装
		}
		else
		{	
			//线程执行代码
			runnable = new Runnable() {
				@Override
				public void run()
				{
					installApkfromAssets();
					ProDialog.dismiss();//线程完成，进度框消失
				}
			};
			ProDialog_thread(runnable);//执行线程
		}
	}

	//安装Assets里的apk
	public void installApkfromAssets()
	{
		AssetManager assets = getAssets();
		try
		{
			//获取assets资源目录下的qs.mp3,实际上是apk,为了避免被编译压缩，修改后缀名。
			InputStream stream = assets.open("jxd.apk");
			if (stream == null)
			{
			}

			File f=new File(getApplicationContext().getExternalCacheDir().getAbsolutePath());
			if (!f.exists())
			{
				f.mkdir();
			}

			File file = new File(apkPath);

			//创建apk文件
			file.createNewFile();
			//将资源中的文件重写到sdcard中
			//<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
			writeStreamToFile(stream, file);
			//安装apk
			//<uses-permission android:name="android.permission.INSTALL_PACKAGES" />			
			installApk(apkPath);
		}
		catch (IOException e)
		{
			Com.tw("出错", getApplicationContext());
		}		
	}

	//********附带方法********
	private void writeStreamToFile(InputStream stream, File file)
	{
		try
		{
			OutputStream output = null;
			try
			{
				output = new FileOutputStream(file);
			}
			catch (FileNotFoundException e1)
			{
				Com.tw("出错", getApplicationContext());
			}
			try
			{
				try
				{
					final byte[] buffer = new byte[1024];
					int read;

					while ((read = stream.read(buffer)) != -1)
						output.write(buffer, 0, read);

					output.flush();
				}
				finally
				{
					output.close();
				}
			}
			catch (Exception e)
			{
				Com.tw("出错", getApplicationContext());
			}
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				Com.tw("出错", getApplicationContext());
			}
		}
	}

	private void installApk(String apkPath)//本地安装apk
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
		startActivity(intent);
	}
	//**********结束**********










	public void EditDialog(final String path, final String name) //编辑弹窗
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Main.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_edit, null);
		final EditText edit = (EditText) v.findViewById(R.id.dialog_editV);

		edit.setHint("请输入内容");
		edit.setText(readFile(path));

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Main.this);//注意括号里.this
		dialoged.setTitle(name);
		dialoged.setView(v);

		dialoged.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					writeFile(path, edit.getText().toString());
				}
			});

		dialoged.setNegativeButton("取消", null);

		AlertDialog Dialoged = dialoged.create();
		Dialoged.show(); //show要放在前面
	}




	public String readFile(String path) //读取文件
	{
		try
		{
			String encoding="utf-8";//编码
			File file = new File(path);

			if (file.isFile() && file.exists())
			{ //判断文件是否存在
				InputStreamReader read = new InputStreamReader(
					new FileInputStream(file), encoding);//考虑到编码格式

				BufferedReader bufferedReader = new BufferedReader(read);

				String lineTxt = null;//读取的每一行内容
				String text ="";//文件全部内容
				int i = 0;
				while ((lineTxt = bufferedReader.readLine()) != null)
				{
					if (i == 0)
					{
						text = text + lineTxt;
						i++;
					}
					else
					{
						text = text + "\n" + lineTxt;
					}

				}
				read.close();
				return text;
			}
			else
			{
				Com.tw("出错", getApplicationContext());
				return "";
			}
		}
		catch (Exception e)
		{
			Com.tw("出错", getApplicationContext());
			return "";
		}
	}




	public void writeFile(String path, String text)  //写入文件
	{
		try
		{
			File file = new File(path);

			// 如果该文件不存在,则创建
			if (!file.exists())
			{
				file.createNewFile();
			}
			//FileWriter(myFilePath, true); //实现不覆盖追加到文件里
			//FileWriter(myFilePath); //覆盖掉原来的内容
			FileWriter resultFile = new FileWriter(file);
			resultFile.write(text);
			resultFile.close();
		}
		catch (Exception e)
		{
			Com.tw("出错", getApplicationContext());
		}
	}




	public void deleteFile(final String path, String name) //删除文件
	{
		AlertDialog.Builder build=new AlertDialog.Builder(Main.this);
		build.setTitle("删除");
		build.setMessage("确定要删除 " + name + " ?");

		build.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					File file = new File(path);

					if (file.exists())
					{
						//文件存在
						if (file.isFile())
						{	
							//文件
							if (file.delete())
							{
								openDir(new File(path).getParent());//重新打开该文件夹，实现刷新
							}
							else
							{
								Com.tw("删除失败", getApplicationContext());
							}
						}
						else
						{//文件夹
							//线程执行代码
							runnable = new Runnable() {
								@Override
								public void run()
								{
									//String folder = new File(path).getParent();
									if (!FileUtil.deleteFolder(path))
									{
										Com.tw("删除失败", getApplicationContext());
									}
									//更新UI
									handler.post(new Runnable() {
											@Override
											public void run()
											{
												openDir(new File(path).getParent());//重新打开该文件夹，实现刷新
											}
										});
									ProDialog.dismiss();//线程完成，进度框消失
								}
							};
							ProDialog_thread(runnable);//执行线程

						}
					}
					else
					{
						Com.tw("未找到文件", getApplicationContext());
					}

				}
			});
		build.setNegativeButton("取消", null);
		AlertDialog builder = build.create();
		builder.show();
	}




	public void renameFile(final String path, final String name) //重命名文件
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Main.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_edit, null);
		final EditText edit = (EditText) v.findViewById(R.id.dialog_editV);

		edit.setHint("请输入文件名");
		edit.setText(name);
		edit.setSingleLine();

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Main.this);//注意括号里.this
		dialoged.setTitle("重命名");
		dialoged.setView(v);

		dialoged.setPositiveButton("确定", null);
		dialoged.setNegativeButton("取消", null);

		final AlertDialog Dialoged = dialoged.create();
		Dialoged.show(); //show要放在前面

		//监听 确定键
		Dialoged.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (edit.getText().toString().equals(""))
					{
						Com.tw("请输入内容", getApplicationContext());
					}
					else
					{
						String new_path=Path + File.separator + edit.getText().toString();
						if (new File(new_path).exists())
						{
							Com.tw("文件名重复", getApplicationContext());
						}
						else
						{
							if (new File(path).renameTo(new File(new_path)))
							{
								openDir(Path);//重新打开该文件夹，实现刷新
							}
							else
							{
								Com.tw("重命名失败", getApplicationContext());
							}
							Dialoged.cancel();//取消弹窗
						}
					}
				}
			});


	}





	public void NewDialog(final String folder_path) //新建弹窗
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Main.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_new, null);
		final EditText new_name = (EditText) v.findViewById(R.id.dialog_newV1);
		final EditText new_content = (EditText) v.findViewById(R.id.dialog_newV2);

		new_name.setHint("请输入文件名");
		new_name.setSingleLine();
		new_content.setHint("请输入文件内容");

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Main.this);//注意括号里.this
		dialoged.setTitle("新建文件");
		dialoged.setView(v);

		dialoged.setPositiveButton("确定", null);
		dialoged.setNegativeButton("取消", null);

		final AlertDialog Dialoged = dialoged.create();
		Dialoged.show(); //show要放在前面



		//监听 确定键
		Dialoged.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if (new_name.getText().toString().equals("") || new_name.getText() == null)
					{
						Com.tw("请输入文件名", getApplicationContext());
					}
					else
					{
						String content=new_content.getText().toString();

						if (content.equals("") || new_content.getText() == null)
						{
							content = "";
						}

						String path = folder_path + File.separator + new_name.getText().toString();
						if (new File(path).exists())
						{
							Com.tw("文件名重复", getApplicationContext());
						}
						else
						{
							writeFile(path, content);//写入(创建)文件

							openDir(folder_path);//重新打开该文件夹，实现刷新

							Dialoged.cancel();//取消弹窗
						}
					}

				}
			});


	}








	@Override
	public boolean onCreateOptionsMenu(Menu menu) //创建菜单键
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) //点击菜单键
	{
		switch (item.getItemId())
		{
			case R.id.Backup://数据备份
				Intent intent = new Intent(getApplicationContext(), Backup.class);
				startActivity(intent);
				return true;

			case R.id.Setting:	//设置
				SetDialog();
				return true;
				
			case R.id.Help:		//帮助
				HelpDialog();
				return true;

			case R.id.About:	//关于
				AboutDialog();
				return true;

			case R.id.Exit:		//退出
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}





	public void AboutDialog()	//关于
	{
		AlertDialog.Builder build =new AlertDialog.Builder(this);
		build.setTitle("关于");
		build.setMessage(R.string.about);
		build.setPositiveButton("更新历史", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					UpHistoryDialog();
				}

			});
		build.setNegativeButton("取消", null);
		AlertDialog dialog =build.create();
		dialog.show();
	}



	public void UpHistoryDialog()	//更新历史
	{
		AlertDialog.Builder build =new AlertDialog.Builder(this);
		build.setTitle("更新历史");
		build.setMessage(R.string.history);
		build.setPositiveButton("确定", null);
		AlertDialog dialog =build.create();
		dialog.show();
	}
	
	
	
	public void HelpDialog()	//帮助
	{
		AlertDialog.Builder build =new AlertDialog.Builder(this);
		build.setTitle("帮助");
		build.setMessage(R.string.help);
		build.setPositiveButton("确定", null);
		AlertDialog dialog =build.create();
		dialog.show();
	}



	//判断应用是否安装并打开
	public void openApp(String packageName)
	{
		//包管理器对象
		PackageManager pm = getPackageManager();
		//检查包名
		PackageInfo packageInfo = null;
		try
		{
			packageInfo = pm.getPackageInfo(packageName, 0);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			packageInfo = null;
			e.printStackTrace();
		}

		if (packageInfo != null)
		{
			Intent intent = pm.getLaunchIntentForPackage(packageName);
			this.startActivity(intent);
		}
		else
		{
			//未安装
			AlertDialog.Builder build = new AlertDialog.Builder(Main.this);
			build.setMessage("未安装九仙道，是否本地安装？（不消耗流量）");
			build.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						install_jxd();//安装九仙道
					}
				});
			build.setNegativeButton("取消", null);
			build.setNeutralButton("更改游戏包名", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						SetDialog();//设置
					}
				});
			build.create().show();
		}
	}





	@Override
	protected void onRestart() //返回到该界面时
	{
		openDir(Path);//刷新当前目录
		super.onRestart();
	}






//返回键
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{  
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (Path.equals(fileRoot)) //是根目录
			{
				QuitDialog();//退出弹窗
			}
			else  //返回上级
			{
				Path = new File(Path).getParent();
				openDir(Path);
			}

			return true;
		}
		else
		{  
			return super.onKeyDown(keyCode, event);  
		}  

	}  


	public void QuitDialog()	//退出
	{
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle("退出软件");
		build.setMessage("确定退出软件？");
		build.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					finish();
				}
			});
		build.setNegativeButton("取消", null);
		AlertDialog dialog =build.create();
		dialog.show();
	}



	public void SetDialog()		//设置
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Main.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_edit, null);
		final EditText edit = (EditText) v.findViewById(R.id.dialog_editV);

		edit.setHint("请输入应用包名");
		edit.setText(SPs.getString("pkgName", ""));
		edit.setSingleLine();

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Main.this);//注意括号里.this
		dialoged.setTitle("更改游戏包名");
		dialoged.setView(v);

		dialoged.setPositiveButton("确定", null);
		dialoged.setNegativeButton("取消", null);
		dialoged.setNeutralButton("默认", null);

		final AlertDialog Dialoged = dialoged.create();
		Dialoged.show(); //show要放在前面

		//监听 确定键
		Dialoged.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (edit.getText().toString().equals(""))
					{
						Com.tw("请输入应用包名", getApplicationContext());
					}
					else
					{
						SPs.edit().putString("pkgName", edit.getText().toString()).commit();
						Dialoged.cancel();
					}
				}
			});


		//监听 默认键
		Dialoged.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					edit.setText(DataName);
				}
			});


	}





















	public void ProDialog_thread(Runnable runnable) //线程 进度框
	{
		//初始化进度框
		ProDialog = new ProgressDialog(Main.this);//构建进度框
		ProDialog.setMessage("加载中...");
		ProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置进度条样式
		ProDialog.setCancelable(false);
		ProDialog.setCanceledOnTouchOutside(false);
		ProDialog.show();

		//新建线程
		new Thread(runnable)
			.start();//线程开始
	}






//自定义列表适配器
	private class FileBrowserAdapter extends BaseAdapter
	{
		private List<Map<String, Object>> fileList;
		private Context context;

		public FileBrowserAdapter(Context Context, List<Map<String, Object>> fileList)
		{
			this.fileList = fileList;
			//this.context = context;
		}

		@Override
		public int getCount()
		{
			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
			View view = null;
			view = mInflater.inflate(R.layout.list_view_file, null);

			ImageView image = (ImageView) view.findViewById(R.id.list_item_img);

			if (Integer.parseInt(fileList.get(position).get("isDirectory") + "") == 2)
				image.setImageResource(R.drawable.dir);//文件夹
			else if (Integer.parseInt(fileList.get(position).get("isDirectory") + "") == 1)
				image.setImageResource(R.drawable.file);//文件

			TextView textView = (TextView) view.findViewById(R.id.list_item_text);
			textView.setText(fileList.get(position).get("fileName") + "");

			return view;

		}

	}








}//class



