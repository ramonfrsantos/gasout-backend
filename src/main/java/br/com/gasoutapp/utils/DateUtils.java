package br.com.gasoutapp.utils;

import java.util.Date;

public class DateUtils {

	public static long differenceInSeconds(Date startDate, Date endDate) {
		long diffInMillies = endDate.getTime() - startDate.getTime();
		return diffInMillies / 1000;
	}
}
