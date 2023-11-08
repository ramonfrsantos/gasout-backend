package br.com.gasoutapp.infrastructure.utils;

import java.util.Date;

public class DateUtils {

	public static long differenceInSeconds(Date startDate, Date endDate) {
		long diffInMillies = endDate.getTime() - startDate.getTime();
		return diffInMillies / 1000;
	}
	
	public static long differenceInMinutes(Date startDate, Date endDate) {
		long diffInMillies = endDate.getTime() - startDate.getTime();
		return diffInMillies / (60 * 1000);
	}
}
