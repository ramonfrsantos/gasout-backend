package br.com.gasoutapp.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gasoutapp.dto.RevisionDetailsDTO;

public class JsonUtil {

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
	}

	public static String ObjectToJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T jsonToObject(String json, Class<T> obj) {

		try {

			return mapper.readValue(json, obj);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static List<RevisionDetailsDTO> addKeysToJsonArray(List<Object[]> list) {
		List<RevisionDetailsDTO> details = new ArrayList<RevisionDetailsDTO>();

		for (Object[] revision : list) {
			RevisionDetailsDTO r = new RevisionDetailsDTO();
			r.setEntity(revision[0]);
			r.setRevisionDetails(revision[1]);
			r.setRevisionType(revision[2]);
			r.setUpdatedAttributes(revision[3]);
			details.add(r);
		}

		return details;
	}
}
