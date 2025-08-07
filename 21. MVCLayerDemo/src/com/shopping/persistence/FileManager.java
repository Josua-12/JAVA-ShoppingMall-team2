package com.shopping.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.shopping.model.User;

/*
 * 파일 입출력을 담당하는 유틸리티 클래스 
 * Persistence Layer의 핵심 컴포넌트 
 * 	- 
 */
public class FileManager {
	//data 디렉토리 경로
	private static final String DATA_DIR = "data";

	/*
	 * 파일에서 객체 리스트 읽기
	 */
	public static <T> List<T> readFromFile(String fileName) {
		//파일 경로
		String fullPath = normalizePath(fileName);
		File file = new File(fileName);
		
		//파일이 존재하지 않는 경우
		if(!file.exists()) {
			System.out.println("파일이 존재하니 않습니다: " + fullPath);
			System.out.println("빈 리스트를 반환합니다.");
			return new ArrayList<>();
		}
		
		//파일이 비어있는 경우
		if(file.length() == 0) {
			System.out.println("파일이 비어있습니다: " + fullPath);
			return new ArrayList<>();
		}
		
		//파일 읽기
		try(ObjectInputStream ois = new ObjectInputStream(
									new FileInputStream(file))) {
			List<T> data = (List<T>) ois.readObject();
			System.out.println("파일 읽기 성공: " +fullPath+ 
											" (" +data.size()+ "개 항목");
			return data;
			
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
			return new ArrayList<>();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ArrayList<>();
		}

		
	}

	// 파일 경로 정규화 
	// 운영체제에 관계없이 올바른 경로 반환 
	private static String normalizePath(String fileName) {
		// 이미 경로가 포함된 경우 그대로 반환
		if(fileName.contains(File.separator) || fileName.contains("/") 
								|| fileName.contains("\\")) {
			return fileName;
		}
		
		// data 디렉토리 경로 추가
		return DATA_DIR +File.separator+fileName;
	}

	// 객체 리스트를 파일에 저장 
	public static <T> void writeToFile(String fileName, List<T> data) {
		// null 체크 
		if(data == null) {
			System.err.println("[FileManager] 저장할 데이터가 null입니다.");
			return;
		}
		
		// 파일 경로 
		String fullpath = normalizePath(fileName);
		File file = new File(fullpath);
		
		// 부모 디렉토리 확인 및 생성
		File parentDir = file.getParentFile();
		if(parentDir != null && !parentDir.exists()) {
			Boolean created = parentDir.mkdirs();
			if(created) {
				System.out.println("디렉토리 생성: " + parentDir.getPath());
			}
		}
		
		// 파일 저장 시도
		try(ObjectOutputStream oos = new ObjectOutputStream(
									new FileOutputStream(file))) {
			oos.writeObject(data);
			oos.flush();  	// 버퍼 강제 flush
			
			System.out.println("파일 저장 성공: " +fullpath+ 
										"(" +data.size()+ "개 항목)");
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("파일 저장 실패: " + fullpath);
			System.out.println(" 오류 내용: " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
}


















