package com.testovac;

import java.io.Serializable;

/**
 * Created by Janco1 on 29. 5. 2015.
 */
public class Statistika implements Serializable {

	int vyriesenych;
	int uspesnych;
	double uspesnost;
	int minusBodov;
	int[] pribudlo = new int[5];
	int[] zleZodpovedane;
	boolean test;
	boolean trening;
	boolean ucenie;

}
