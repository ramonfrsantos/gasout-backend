package br.com.gasoutapp.utils;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

	public static String createRandomCode(int tamanhoCodigo, String caracteresUsados) {
		List<Character> grupoCaracteres = caracteresUsados.chars().mapToObj(i -> (char) i).collect(Collectors.toList());
		Collections.shuffle(grupoCaracteres, new SecureRandom());
		return grupoCaracteres.stream().map(Object::toString).limit(tamanhoCodigo).collect(Collectors.joining());
	}
	
	public static String normalizeString(String string) {
		if (string != null) {
			string = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toUpperCase();
		}

		return string;
	}

	public static <T> void reverseList(List<T> list) {
		if (list == null || list.size() <= 1) {
			return;
		}

		T value = list.remove(0);
		reverseList(list);

		list.add(value);
	}
}
