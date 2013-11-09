package com.kurento.kmf.repository.internal.repoimpl.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ItemsMetadata {

	private Logger log = LoggerFactory.getLogger(ItemsMetadata.class);

	// TODO Avoid potential memory leaks using Google's MapMaker
	private ConcurrentMap<String, Map<String, String>> itemsMetadata;

	private File itemsMetadataFile;

	public ItemsMetadata(File itemsMetadataFile) {
		this.itemsMetadataFile = itemsMetadataFile;
		try {
			loadItemsMetadata();
		} catch (IOException e) {
			log.warn("Exception while loading items metadata", e);
		}
	}

	private void loadItemsMetadata() throws IOException {
		DBObject contents = (DBObject) JSON.parse(loadFileAsString());
		itemsMetadata = new ConcurrentHashMap<String, Map<String, String>>();
		for (String key : contents.keySet()) {
			try {
				DBObject metadata = (DBObject) contents.get(key);
				Map<String, String> map = new HashMap<String, String>();
				for (String metadataKey : metadata.keySet()) {
					map.put(key, metadata.get(metadataKey).toString());
				}
			} catch (ClassCastException e) {
				log.warn("Attribute '" + key + "' should be an object");
			}
		}
	}

	private String loadFileAsString() throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(
				new FileReader(itemsMetadataFile));
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();
		return sb.toString();
	}

	public synchronized void setMetadataForId(String id,
			Map<String, String> metadata) {
		itemsMetadata.put(id, metadata);
	}

	public synchronized Map<String, String> loadMetadata(String id) {
		Map<String, String> metadata = itemsMetadata.get(id);
		if (metadata == null) {
			metadata = new HashMap<String, String>();
			itemsMetadata.put(id, metadata);
		}
		return metadata;
	}

	public List<Entry<String, Map<String, String>>> findRepositoryItemsByAttValue(
			String attributeName, String value) {

		List<Entry<String, Map<String, String>>> list = new ArrayList<Map.Entry<String, Map<String, String>>>();

		for (Entry<String, Map<String, String>> item : itemsMetadata.entrySet()) {
			if (item.getValue().get(attributeName).equals(value)) {
				list.add(item);
			}
		}

		return list;
	}

	public List<Entry<String, Map<String, String>>> findRepositoryItemsByAttRegex(
			String attributeName, String regex) {

		Pattern pattern = Pattern.compile(regex);

		List<Entry<String, Map<String, String>>> list = new ArrayList<Map.Entry<String, Map<String, String>>>();

		for (Entry<String, Map<String, String>> item : itemsMetadata.entrySet()) {
			if (pattern.matcher(item.getValue().get(attributeName)).matches()) {
				list.add(item);
			}
		}

		return list;
	}

	public void save() {
		try {
			PrintWriter writer = new PrintWriter(itemsMetadataFile);
			String content = JSON.serialize(itemsMetadata);
			writer.print(content);
			writer.close();
		} catch (IOException e) {
			log.error("Exception writing metadata file", e);
		}
	}
}
