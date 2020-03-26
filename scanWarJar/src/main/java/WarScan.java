package main.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class WarScan {
	
	public static String META_INF = "META-INF";
	
	public static String WEB_INF = "WEB-INF";
	
	public static String SOFA_CONTAINER = "sofa-container";
	
	public static String KERNEL = "kernel";
	
	public static String REPOSITORY = "repository";
	
	public static String JAR_SUFFIX = ".jar";
	
//	public static void main(String[] args) {
//		String excelPath = "F:\\test\\jarversion.xlsx";
//		Map<String, List<JarVersion>> map = new HashMap<String, List<JarVersion>>();
//		List<JarVersion> list = new ArrayList<JarVersion>();
//		JarVersion jar = new JarVersion();
//		jar.setJarVersion("setJarVersion");
//		jar.setJarBuildTime("jarBuildTime");
//		jar.setJarName("setJarName");
//		list.add(jar);
//		map.put("sofa1", list);
//		XLSXUtil.writeToExcel(excelPath, map);
//	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Map<String, List<JarVersion>> map = new HashMap<String, List<JarVersion>>();
		System.out.println("请输入需要扫描的文件夹，如果终止操作 请输入N");
		String filePath = sc.nextLine();
		String excelPath = "";
		while(!"N".equals(filePath)) {
			File file = new File(filePath);
			if (file.exists()) {
				File excelFile = new File(excelPath);
				while (StringUtils.isEmpty(excelPath) || !excelFile.exists() || 
						!(excelFile.getName().contains(".xlsx") || excelFile.getName().contains(".xls"))) {
					System.out.println("请输入保存信息的Excel的全路径，文件格式必须是xlsx或者xls，如果终止操作 请输入N");
					excelPath = sc.nextLine();
					excelFile = new File(excelPath);
				}
				scanDir(file, map);
				XLSXUtil.writeToExcel(excelPath, map);
				
				System.out.println("如果需要继续扫描，请输入需要扫描的文件夹全路径，如果终止操作 请输入N");
				filePath = sc.nextLine();
			} else {
				System.out.println("请重新输入需要扫描的文件夹全路径，如果终止操作 请输入N");
			}
			
		}
		
		if (sc != null) {
			sc.close();
		}
	}
	
	/**
	 * 扫描文件夹，找war文件
	 * @param file 需要扫描的文件夹
	 * @param list jar的版本信息
	 */
	public static void scanDir(File file, Map<String, List<JarVersion>> map) {
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				if (file2.getName().contains(".war")) {
					try {
						ShellUtil.exec("jar xvf " +  file2.getName(), file);
						
						//添加war信息
						List<JarVersion> list = map.get(file2.getName());
						list = (list == null ? new ArrayList<JarVersion>() : list);
						//读取war包中的jar 信息
						readWebInf(file, list);
						
						map.put(file2.getName(), list);
						
						//删除解压的文件
						deleteFiles(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (file2.isDirectory()){
					if (file2.getName().equals("database") || file2.getName().equals("Documents") || file2.getName().equals("src")) {
						//这些文件不需要扫描
						continue;
					}
					scanDir(file2, map);
				}
			}
		}
	}
	
	/**
	 * 删除解压的信息
	 * @param file 需要删除的文件file
	 */
	public static void deleteFiles(File file) {
		if (file.isDirectory()){
				File[] listFiles = file.listFiles();
				for (File file2 : listFiles) {
					if (file2.getName().equals(WEB_INF) || file2.getName().equals(META_INF)) {
						deleteFile(file2);
					}
				}
			}
	}
	
	/**
	 * 迭代删除文件
	 * @param file 文件
	 */
	public static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				deleteFile(file2);
			}
			file.delete();
		} else {
			file.delete();
		}
	}
	
	
	/**
	 * 读取web-inf 下的信息
	 * @param rootFile
	 * @param list
	 */
	public static void readWebInf(File rootFile, List<JarVersion> list) {
		File[] listFiles = rootFile.listFiles();
		for (File subFile : listFiles) {
			//读取jar包的信息
			if (subFile.getName() != null && subFile.getName().contains(WEB_INF)) {
				File[] listF = subFile.listFiles();
				for (File file : listF) {
					if (file != null && file.getName() != null && file.getName().equals(SOFA_CONTAINER)) {
						//目前只读取kernel 和repository文件夹下的内容
						File[] listFiles2 = file.listFiles();
						for (File file2 : listFiles2) {
							if (file2.exists() && 
									(file2.getName().equals(KERNEL) || file2.getName().equals(REPOSITORY)) && file2.isDirectory()) {
								readFileVersion(file2, list);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * 读文件，获取里面的版本和构建信息
	 * @param file 传入的文件信息
	 * @param keyword 输入的关键字
	 * @return 版本和构建信息
	 */
	public static void readFileVersion (File file, List<JarVersion> list) {
		if (file == null || !file.exists()) {
			return;
		}
		
		//如果是单个的jar文件，直接读取
		if (file.getName() != null && file.getName().contains(JAR_SUFFIX)) {
			readJar(file, list);
		} else if (file.isDirectory() && (!(file.getName().contains("lib") || file.getName().contains("3rd")))){
			//如果是文件夹，循环遍历，查找jar文件
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				readFileVersion(file2, list);
			}
		}
	}
	
	/**
	 * 传入需要读取的jar文件，将 Built-At、 Bundle-Version 的信息返回。
	 * @param file 传入jar文件信息
	 * @return 版本信息
	 */
	public static void readJar(File file, List<JarVersion> list) {
		if (file.exists() && file.getName().contains(JAR_SUFFIX)) {
			try {
				JarVersion jarVersion = new JarVersion();
				JarFile jarFile = new JarFile(file);
				Manifest manifest = jarFile.getManifest();
				Attributes attributes = manifest.getMainAttributes();
				String value = attributes.getValue("Built-At");
				jarVersion.setJarBuildTime(value);
				String value1 = attributes.getValue("Bundle-Version");
				jarVersion.setJarVersion(value1);
				
				String time = "";
				if (!StringUtils.isEmpty(value)) {
					time = value.replaceAll("-", "").replaceAll(":", "");
					
				}
				jarVersion.setJarName(file.getName() + (StringUtils.isEmpty(time) ? "" : "_" + time));
				
				list.add(jarVersion);
				if (jarFile != null) {
					jarFile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
