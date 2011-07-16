package com.merespondeaqui.calculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import net.java.dev.eval.Expression;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.Utils;

public class CalculatorProcessor implements Processor {

	private static final String PREFIX = "oresultadode";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	private static final int SCALE = 3;
	private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

	@Override
	public void process(Tweet tweet, Twitter twitter) throws TwitterException {
		String text = tweet.getText();

		String expr = text.substring(FULL_PREFIX.length() + 1);

		Expression exp = new Expression(expr);
		Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
		BigDecimal result = exp.eval(variables);
		
		result.setScale(SCALE, BigDecimal.ROUND_HALF_UP);

		TwitterUtils.reply("Resultado: '" + FORMAT.format(result) + "'", 
				tweet, twitter);
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

}
