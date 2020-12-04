package com.jxd.fc.fuzhu;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.utils.*;
import java.io.*;
import java.text.*;
import java.util.*;

import android.view.View.OnClickListener;

import android.support.v7.app.*;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.*;
import android.text.*;

public class Backup extends AppCompatActivity 
{
	private List<Map<String,Object>> fileList = new ArrayList<Map<String,Object>>();
	private ListView ListB;
	private String Path;
	private String fileRoot;
	private Common Com;
	private String jsPath;
	private ZipUtil zip;
	private String DataName;

	private ProgressDialog ProDialog;
	private Runnable runnable;
	private Handler handler;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.backup);

		Com = new Common();
		zip = new ZipUtil();
		handler = new Handler();
		DataName = "com.jxd.fc";

		ListB = (ListView) findViewById(R.id.list_bak);//列表

		//初始化路径
		fileRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + DataName;
		jsPath = fileRoot + File.separator + "js";
		Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/backup/jxd";


		openDir(Path);//打开文件夹



		//列表点击事件
		ListB.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					boolean All=false;
					if (fileList.get(p3).get("isAll").toString().equals("true"))//全部备份
					{
						All = true;
					}
					String filepath = Path + File.separator + fileList.get(p3).get("fileName") + "";//文件路径
					String fileName = fileList.get(p3).get("fileName") + "";//文件名

					if (!All)//角色还原
					{
						File files = new File(fileRoot);
						if (!files.exists() || !files.isDirectory() || files.list().length == 0)//判断文件夹是否存在
						{
							Com.tw("无游戏数据，无法还原角色备份数据", getApplicationContext());
						}
						else
						{
							recoverBak(filepath, fileName, All);
						}
					}
					else
					{
						recoverBak(filepath, fileName, All);
					}

				}
			});




		//列表长按事件
		ListB.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					String filePath = Path + File.separator + fileList.get(p3).get("fileName") + "";//文件路径
					String fileName = fileList.get(p3).get("fileName") + "";//文件名
					SelDialog(filePath, fileName);

					return true; //返回true，就不执行列表点击事件
				}

			});





		//悬浮按钮代码
        FloatingActionButton FloatB = (FloatingActionButton) findViewById(R.id.FloatBtn);
        FloatB.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view)
				{
					File files = new File(jsPath);
					if (!files.exists() || !files.isDirectory() || files.list().length == 0)//判断文件夹是否存在
					{
						//Com.tw("无游戏角色数据", getApplicationContext());
						AlertDialog.Builder build = new AlertDialog.Builder(Backup.this);
						build.setMessage("无游戏角色数据，是否全部备份？");
						build.setPositiveButton("确定", new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									AllBackup("全部备份"+System.currentTimeMillis());//全部备份
								}
							});
						build.setNegativeButton("取消", null);
						build.create().show();
					}
					else
					{
						NewDialog();
					}
				}
			});







	}//oncreate











	public void openDir(String Dir) //打开文件夹
	{
		LinearLayout LL3=(LinearLayout) findViewById(R.id.LinearLayout3);//列表
		LinearLayout LL4=(LinearLayout) findViewById(R.id.LinearLayout4);

		File files = new File(Dir);

		if (!files.exists() || !files.isDirectory() || files.list().length == 0)//判断文件夹是否存在，是否有备份
		{
			LL4.setVisibility(View.VISIBLE);
			LL3.setVisibility(View.GONE);
		}
		else
		{
			LL3.setVisibility(View.VISIBLE);
			LL4.setVisibility(View.GONE);

			fileList = GetPathFilsList(Dir);

			FileBrowserAdapter phoneFileBrowserAdapter = new FileBrowserAdapter(getApplicationContext(), fileList);

			ListB.setAdapter(phoneFileBrowserAdapter);
		}

	}




	public static List<Map<String,Object>> GetPathFilsList(String path)
	{
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> filelist = new ArrayList<Map<String,Object>>();

		try
		{
			File filepath;
			String[] Files = new File(path).list();
			for (String file : Files)
			{
				Map<String, Object> map = new HashMap<String, Object>();

				filepath = new File(path + File.separator + file);

				//取后缀名
				String file_name=filepath.getName();
				String suffix=file_name.substring(file_name.lastIndexOf(".") + 1);

				if (suffix.equals("all"))//属于全部备份
				{
					map.put("isAll", "true");
					map.put("fileName", file);

					list.add(map);
				}
				else
				{
					map.put("isAll", "false");
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
	}



	public void SelDialog(final String path, final String name) //选择项 弹窗
	{
		AlertDialog.Builder build = new AlertDialog.Builder(Backup.this);
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






	public void recoverBak(final String path, final String name, final boolean isAll) //备份还原
	{
		AlertDialog.Builder build=new AlertDialog.Builder(Backup.this);
		build.setTitle("还原");
		build.setMessage("确定将备份数据还原？");
		build.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if (isAll)//全部备份
					{
						//线程执行代码
						runnable = new Runnable() {
							@Override
							public void run()
							{
								zip.unZiFiles(path, fileRoot);
								ProDialog.dismiss();//线程完成，进度框消失
							}
						};
						ProDialog_thread(runnable);
					}
					else
					{
						//线程执行代码
						runnable = new Runnable() {
							@Override
							public void run()
							{
								String suffix=name.substring(name.lastIndexOf(".") + 1);
								zip.unZiFiles(path, jsPath + "/" + suffix);
								ProDialog.dismiss();//线程完成，进度框消失
							}
						};
						ProDialog_thread(runnable);
					}
				}
			});
		build.setNegativeButton("取消", null);
		build.create().show();
	}



	public void renameFile(final String path, String name) //重命名文件
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Backup.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_edit, null);
		final EditText edit = (EditText) v.findViewById(R.id.dialog_editV);

		//取后缀名
		final String suffix=name.substring(name.lastIndexOf(".") + 1);
		//取前缀名
		String prefix=name.substring(0, name.lastIndexOf("."));

		edit.setHint("请输入文件名");
		edit.setText(prefix);
		edit.setSingleLine();

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Backup.this);//注意括号里.this
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
						String new_path=Path + File.separator + edit.getText().toString() + "." + suffix;
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



	public void deleteFile(final String path, String name) //删除文件
	{
		AlertDialog.Builder build=new AlertDialog.Builder(Backup.this);
		build.setTitle("删除");
		build.setMessage("确定要删除 " + name + " ?");

		build.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					File file = new File(path);

					// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
					if (file.exists() && file.isFile())
					{
						if (file.delete())
						{
							openDir(Path);//重新打开该文件夹，实现刷新
						}
						else
						{
							Com.tw("删除失败", getApplicationContext());
						}
					}
					else
					{
						Com.tw("出错", getApplicationContext());
					}

				}
			});
		build.setNegativeButton("取消", null);
		build.create().show();
	}










	public void NewDialog() //新建备份弹窗
	{
		//自定义弹窗
		LayoutInflater inf = LayoutInflater.from(Backup.this);//注意括号里.this
		View v = inf.inflate(R.layout.dialog_newbak, null);
		final EditText bak_name = (EditText) v.findViewById(R.id.dialog_newbakV1);
		final ListView js_list = (ListView) v.findViewById(R.id.dialog_newbakV2);//角色列表
		final TextView js_name = (TextView) v.findViewById(R.id.dialog_newbakV3);

		bak_name.setHint("请输入备份文件名");
		bak_name.setSingleLine();
		js_list.setAdapter(JSlist());

		AlertDialog.Builder dialoged = new AlertDialog.Builder(Backup.this);//注意括号里.this
		dialoged.setTitle("数据备份");
		dialoged.setView(v);

		dialoged.setPositiveButton("备份", null);
		dialoged.setNeutralButton("全部备份", null);
		dialoged.setNegativeButton("取消", null);

		final AlertDialog Dialoged = dialoged.create();
		Dialoged.show(); //show要放在前面


		//备份
		Dialoged.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if (bak_name.getText().toString().equals(""))
					{
						Com.tw("请输入备份文件名", getApplicationContext());
					}
					else
					{
						if (js_name.getText().equals("未选择"))
						{
							Com.tw("请选择角色", getApplicationContext());
						}
						else
						{
							final String bakName = Path + "/" + bak_name.getText().toString() + "." + js_name.getText();//备份文件路径
							if (new File(bakName).exists())
							{
								Com.tw("文件名重复", getApplicationContext());
							}
							else
							{
								File files = new File(Path);

								if (!files.exists())//判断文件夹是否存在
								{
									files.mkdirs();
								}

								//线程执行代码
								runnable = new Runnable() {
									@Override
									public void run()
									{
										try
										{
											zip.zipFiles(jsPath + "/" + js_name.getText(), bakName);
											//更新UI
											handler.post(new Runnable() {
													@Override
													public void run()
													{
														openDir(Path);
													}
												});

										}
										catch (IOException e)
										{
											Com.tw("出错", getApplicationContext());
										}


										ProDialog.dismiss();//线程完成，进度框消失
									}
								};
								ProDialog_thread(runnable);//执行线程


								Dialoged.cancel();//取消弹窗
							}
						}
					}

				}
			});



		//全部备份
		Dialoged.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if (bak_name.getText().toString().equals(""))
					{
						Com.tw("请输入备份文件名", getApplicationContext());
					}
					else
					{
						String bakName = Path + "/" + bak_name.getText().toString() + ".all";//备份文件路径
						if (new File(bakName).exists())
						{
							Com.tw("文件名重复", getApplicationContext());
						}
						else
						{
							AllBackup(bak_name.getText().toString());//全部备份
							Dialoged.cancel();//弹窗取消
						}
					}
				}
			});



		//角色列表 点击事件
		js_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v, int position, long id)
				{
					String name = (String) l.getItemAtPosition(position);
					if (bak_name.getText().toString().equals(""))
					{
						bak_name.setText(name);
					}
					js_name.setText(name);
				}
			});



	}




	public void AllBackup(String name)//全部备份
	{
		File files = new File(Path);

		if (!files.exists())//判断文件夹是否存在
		{
			files.mkdirs();
		}

		final String bakName = Path + "/" + name + ".all";//备份文件路径
		
		//线程执行代码
		runnable = new Runnable() {
			@Override
			public void run()
			{
				try
				{
					zip.zipFiles(fileRoot, bakName);
					//更新UI
					handler.post(new Runnable() {
							@Override
							public void run()
							{
								openDir(Path);
							}
						});
				}
				catch (IOException e)
				{
					Com.tw("出错", getApplicationContext());
				}

				ProDialog.dismiss();//线程完成，进度框消失
			}
		};
		ProDialog_thread(runnable);//执行线程
	}



	public ListAdapter JSlist()//角色列表
	{
		File file = new File(jsPath);
		String[] files=file.list();

		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);

		return adapter;
	}




	public void ProDialog_thread(Runnable runnable) //线程 进度框
	{
		//初始化进度框
		ProDialog = new ProgressDialog(Backup.this);//构建进度框
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
			View view = mInflater.inflate(R.layout.list_view_bak, null);

			//标题
			TextView title = (TextView) view.findViewById(R.id.list_itembak_t1);
			//文件名称
			String name = fileList.get(position).get("fileName") + "";
			//取前缀名
			String prefix=name.substring(0, name.lastIndexOf("."));
			title.setText(prefix);

			//内容
			TextView content = (TextView) view.findViewById(R.id.list_itembak_t2);
			//取后缀名
			String suffix=name.substring(name.lastIndexOf(".") + 1);
			//备份类型角色
			String type;
			if (suffix.equals("all"))
				type = "全部数据备份";
			else
				type = "备份角色：" + suffix;
			//文件路径
			File filepath = new File(Path + File.separator + fileList.get(position).get("fileName") + "");
			content.setText(type + "		" + FileTime(filepath));//备份type+文件时间


			return view;
		}

	}





	public static String FileTime(File file)
	{
		String result = "";

		long time=file.lastModified(); //文件最后一次修改时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		result = formatter.format(time);

		return result;
	}





}//class







