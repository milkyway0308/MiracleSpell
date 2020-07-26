package com.nisovin.magicspells.util.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.RandomColorHexCodeDecimalResolver;
import com.nisovin.magicspells.variables.Variable;

public class Expression {

	enum OperationType {
		ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, MINIMUM, MAXIMUM, ABSOLUTE_VALUE, MODULUS, AVERAGE
		//TODO add greatest common divisor
		//TODO add the least common multiple
		//TODO add the floor function
		//TODO add the ceiling function
		//TODO add a rounding function
	}
	
	private static Map<String, OperationType> opKeys = new HashMap<String, OperationType>();
	static {
		opKeys.put("+", OperationType.ADD);
		opKeys.put("-", OperationType.SUBTRACT);
		opKeys.put("*", OperationType.MULTIPLY);
		opKeys.put("/", OperationType.DIVIDE);
		opKeys.put("^", OperationType.POWER);
		opKeys.put("minimum", OperationType.MINIMUM);
		opKeys.put("maximum", OperationType.MAXIMUM);
		opKeys.put("abs", OperationType.ABSOLUTE_VALUE);
		opKeys.put("%", OperationType.MODULUS);
		opKeys.put("average", OperationType.AVERAGE);
	}
	
	enum UnparameterizedValueResolverType {
		PI, RANDOM_SIGN, RANDOM_VALUE, E, RANDOM_COLOR_DECIMAL, FOOD_LEVEL,
		PLAYER_LOCATION_X, PLAYER_LOCATION_Y, PLAYER_LOCATION_Z, SATURATION_LEVEL
		//add xp level
		//add exp points
		//add health points left
		//add max health
		//add mana
		//add magicxp
		//add light level
		//add number of players online
		//add player velocity magnitude
		//add walk speed
		//add fly speed
		//fall distance
		//first played
		//fire ticks
		//player time
		//remaining air
		//TODO add the golden ratio
	}
	
	private static Map<String, UnparameterizedValueResolverType> upValueResolverKeys = new HashMap<String, Expression.UnparameterizedValueResolverType>();
	
	static {
		upValueResolverKeys.put("pi", UnparameterizedValueResolverType.PI);
		upValueResolverKeys.put("randomsign", UnparameterizedValueResolverType.RANDOM_SIGN);
		upValueResolverKeys.put("randomvalue", UnparameterizedValueResolverType.RANDOM_VALUE);
		upValueResolverKeys.put("e", UnparameterizedValueResolverType.E);
		upValueResolverKeys.put("randomcolordecimal", UnparameterizedValueResolverType.RANDOM_COLOR_DECIMAL);
		upValueResolverKeys.put("player:food:level", UnparameterizedValueResolverType.FOOD_LEVEL);
		upValueResolverKeys.put("player:food:saturation", UnparameterizedValueResolverType.SATURATION_LEVEL);
		upValueResolverKeys.put("player:location:x", UnparameterizedValueResolverType.PLAYER_LOCATION_X);
		upValueResolverKeys.put("player:location:y", UnparameterizedValueResolverType.PLAYER_LOCATION_Y);
		upValueResolverKeys.put("player:location:z", UnparameterizedValueResolverType.PLAYER_LOCATION_Z);
	}
	
	private ValueResolver getUnparameterizedResolver(String key) {
		if (key == null) {
			return null;
		}
		if (upValueResolverKeys == null) {
			return null;
		}
		UnparameterizedValueResolverType type = upValueResolverKeys.get(key.toLowerCase()); 
		if (type == null) {
			return null;
		}
		switch (type) {
		case PI:
			return new PiValueResolver();
		case RANDOM_SIGN:
			return new RandomSignResolver();
		case RANDOM_VALUE:
			return new RandomValueResolver();
		case E:
			return new EValueResolver();
		case RANDOM_COLOR_DECIMAL:
			return new RandomColorHexCodeDecimalResolver();
		case FOOD_LEVEL:
			return new FoodLevelValueResolver();
		case SATURATION_LEVEL:
			return new SaturationLevelValueResolver();
		case PLAYER_LOCATION_X:
			return new PlayerLocationXValueResolver();
		case PLAYER_LOCATION_Y:
			return new PlayerLocationYValueResolver();
		case PLAYER_LOCATION_Z:
			return new PlayerLocationZValueResolver();
		default:
			return null;
		}
	}
	
	private Operation getOperation(OperationType type) {
		switch (type) {
		case ADD:
			return new AdditionOperation();
		case SUBTRACT:
			return new SubtractionOperation();
		case MULTIPLY:
			return new MultiplicationOperation();
		case DIVIDE:
			return new DivisionOperation();
		case POWER:
			return new PowerOperation();
		case MINIMUM:
			return new MinimumOperation();
		case MAXIMUM:
			return new MaximumOperation();
		case ABSOLUTE_VALUE:
			return new AbsoluteValueOperation();
		case MODULUS:
			return new ModulusOperation();
		case AVERAGE:
			new AverageOperation();
		default:
			return null;
		}
	}
	
	
	private ValueResolver term1Resolver = null;
	private Operation operator = null;
	private ValueResolver term2Resolver = null;
	
	static private Pattern chatVarMatchPattern = Pattern.compile("%var:[A-Za-z0-9_]+(:[0-9]+)?%", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	//TODO make sure this is the right expression
	// it is suppose to look for a number and that the whole section matches
	private static Pattern constantValueMatchPattern = Pattern.compile("[0-9]++");
	
	public Expression(String expression) {
		String[] splits = expression.split(" ");
		// for each split
		// see if it is an operation
		// if it is an operation, grab it and save it
		// if it isn't an operation or if the saved op isn't null
		// see if it can be resolved as a value resolver
		// if it can be resolved as a value resolver
		// save it as resolver 1 if resolver 1 is currently null
		// if it can be resolved and resolver 1 isn't null be resolver 2 is
		// save it as resolver 2
		
		for (String split: splits) {
			loadField(split);
		}		
		if (operator == null) {
			MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Operator was null");
			
			operator = new AdditionOperation();
		}
		if (term1Resolver == null) {
			MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Term1 was null");
			term1Resolver = new ConstantResolver(0);
		}
		
		if (term2Resolver == null) {
			MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Term2 was null");
			term2Resolver = new ConstantResolver(0);
		}
		
		// so far, this only resolves up to 2 value resolvers and 1 operation
	}
	
	private void loadField(String split) {
		if (split == null || split.isEmpty()) {
			MagicSpells.error("split is empty or null");
		}
		if (isOperation(split) && operator == null) {
			operator = getOperation(opKeys.get(split.toLowerCase()));
			return;
		}
		ValueResolver r = resolveValueResolver(split);
		if (r != null) {
			if (term1Resolver == null) {
				term1Resolver = r;
				return;
			}
			if (term2Resolver == null) {
				term2Resolver = r;
				return;
			}
		}
		
	}
	
	private boolean isOperation(String split) {
		return opKeys.containsKey(split.toLowerCase());
	}
	
	public Number resolveValue(String playername, Player player) {
		Number ret = operator.evaluate(term1Resolver, term2Resolver, playername, player, null, null);
		return ret;
	}
	
	public Number resolveValue(String playername, Player player, Location loc1, Location loc2) {
		return operator.evaluate(term1Resolver, term2Resolver, playername, player, loc1, loc2);
	}
	
	private ValueResolver resolveValueResolver(String s) {
		if (s == null) {
			return null;
		}
		//try to get a variable resolver from this
		Variable v = isVariable(s);
		if (v != null) {
			return new VariableResolver(v);
		}
		
		//try to resolve as a constant value resolver
		ConstantResolver cr = isConstantValue(s);
		if (cr != null) {
			MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Returning constant resolver from resolveValueResolver");
			return cr;
		}
		MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Constant resolver returned null");
		
		//try resolving the unparameterized resolvers
		ValueResolver upVR = getUnparameterizedResolver(s);
		if (upVR != null) {
			return upVR;
		}
		
		//TODO attempt the other resolvers
		return null;
	}
	
	private Variable isVariable(String input) {
		Matcher matcher = chatVarMatchPattern.matcher(input);
		if (matcher.find()) {
			String varText = matcher.group();
			String[] varData = varText.substring(5, varText.length() - 1).split(":");
			return MagicSpells.getVariableManager().getVariable(varData[0]);
		}
		return null;
	}
	
	private ConstantResolver isConstantValue(String input) {
		MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Trying to resolve \"" + input + "\" as a constant");
		Matcher matcher = constantValueMatchPattern.matcher(input);
		if (matcher.find()) {
			String matchText = matcher.group();
			MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Creating constant resolver with text of \"" + matchText + "\"");
			return new ConstantResolver(Double.parseDouble(matchText));
		}
		return null;
	}
			
/*	static public String doVariableReplacements(Player player, String string) {
		if (string != null && MagicSpells.getVariableManager() != null && string.contains("%var")) {
			Matcher matcher = chatVarMatchPattern.matcher(string);
			while (matcher.find()) {
				String varText = matcher.group();
				String[] varData = varText.substring(5, varText.length() - 1).split(":");
				double val = MagicSpells.getVariableManager().getValue(varData[0], player);
				String sval = varData.length == 1 ? Util.getStringNumber(val, -1) : Util.getStringNumber(val, Integer.parseInt(varData[1]));
				string = string.replace(varText, sval);
			}
		}
		return string;
	} */
}