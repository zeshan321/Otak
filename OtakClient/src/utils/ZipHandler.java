package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipHandler {

	private String zipFilePath;
	private String outputFolder;
	private int BUFFER_SIZE = 4096;


	public ZipHandler(String zipFilePath, String outputFolder) {
		this.zipFilePath = zipFilePath;
		this.outputFolder = outputFolder;
	}

	public void unzip() throws IOException {		
		ZipFile zfile = new ZipFile(new File(zipFilePath));
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(outputFolder, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				InputStream in = zfile.getInputStream(entry);
				try {
					copy(in, file);
				} finally {
					in.close();
				}
			}
		}
	}

	private void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			
			while (true) {
				int readCount = in.read(buffer);
				if (readCount < 0) {
					break;
				}
				out.write(buffer, 0, readCount);
			}
		} finally {
			out.close();
		}
	}
}
