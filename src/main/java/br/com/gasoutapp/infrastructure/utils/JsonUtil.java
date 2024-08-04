package br.com.gasoutapp.infrastructure.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonUtil {

	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
	}

	public static String objectToJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("Error = {}", e.getMessage());
			return null;
		}
	}
	
	public static Object[] convertToObjectArray(Object array) {
	    Class<?> ofArray = array.getClass().getComponentType();
	    if (ofArray.isPrimitive()) {
	        List<Object> ar = new ArrayList<>();
	        int length = Array.getLength(array);
	        for (int i = 0; i < length; i++) {
	            ar.add(Array.get(array, i));
	        }
	        return ar.toArray();
	    }
	    else {
	        return (Object[]) array;
	    }
	}
}
