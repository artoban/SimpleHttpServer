package com.happylife.demo;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class Handlers {
	public static class RootHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + Main.port + "</h1>";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static class EchoHeaderHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			Headers headers = he.getRequestHeaders();
			Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
			String response = "";
			for (Map.Entry<String, List<String>> entry : entries)
				response += entry.toString() + "\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}

	public static class EchoGetHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			// parse request
			Map<String, Object> parameters = new HashMap<String, Object>();
			URI requestedUri = he.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			// send response
			String response = "";
			for (String key : parameters.keySet())
				response += key + " = " + parameters.get(key) + "\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}

	public static class EchoPostHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Served by /echoPost handler...");
		}
	}


	public static class EchoPutHandler implements HttpHandler
	{
		private HSSFWorkbook hwb = new HSSFWorkbook();
		private int pageNumber = 0;
		private HSSFSheet sheet = null;

		public void saveAsExcelFile(ArrayList arList, int pageCounter) {

			try
			{
				if(pageCounter > pageNumber) {
					sheet = hwb.createSheet("new sheet" + String.valueOf(pageCounter));
				}

				for(int k=0;k<arList.size();k++)
				{
					ArrayList ardata = (ArrayList)arList.get(k);
					HSSFRow row = sheet.createRow((short) 0+k);
					for(int p=0;p<ardata.size();p++)
					{
						HSSFCell cell = row.createCell((short) p);
						String data = ardata.get(p).toString();
						if(data.startsWith("=")){
							cell.setCellType(Cell.CELL_TYPE_STRING);
							data=data.replaceAll("\"", "");
							data=data.replaceAll("=", "");
							cell.setCellValue(data);
						}else if(data.startsWith("\"")){
							data=data.replaceAll("\"", "");
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellValue(data);
						}else{
							data=data.replaceAll("\"", "");
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
							cell.setCellValue(data);
						}
						//*/
						// cell.setCellValue(ardata.get(p).toString());
					}
					System.out.println();

				}
				FileOutputStream fileOut = new FileOutputStream("test.xlsx");
				hwb.write(fileOut);
				fileOut.close();
				System.out.println("Your excel file has been generated");
			} catch ( Exception ex ) {
				ex.printStackTrace();
			} //main method ends
		}


		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Served by /echoPut handler...");
			// parse request
			InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");

			// send response
			String response = "";
			try (BufferedReader br = new BufferedReader(isr)) {

				String sCurrentLine;

				ArrayList arList=null;
				ArrayList al=null;
				boolean pageFlag = true;
				String prev = "";
				int pageNumber = 1;

				while ((sCurrentLine = br.readLine()) != null) {
					System.out.println(sCurrentLine);

					if(pageFlag == true) {
						arList = new ArrayList();
					}

					al = new ArrayList();
					String strar[] = sCurrentLine.split(",");

					for(int j=0;j<strar.length;j++) {
						if (!strar[j].isEmpty()) {
							al.add(strar[j]);
							pageFlag = false;
						} else if (strar[j].isEmpty() && !prev.isEmpty()) {
							saveAsExcelFile(arList, pageNumber);
							pageFlag = true;
							strar[j] = "";
							prev = "";////??
						///	pageNumber++;
						}
					}

					if(!strar[0].isEmpty()) {
						arList.add(al);
						prev = al.toString();
					}
				}

				pageNumber++;

				if(!arList.isEmpty()) {
					saveAsExcelFile(arList, pageNumber);
				}

				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}


			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();

		}
	}

	@SuppressWarnings("unchecked")
	public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");

			for (String pair : pairs) {
				String param[] = pair.split("[=]");

				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);
					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}
}
