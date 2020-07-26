package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import com.nisovin.magicspells.castmodifiers.Condition;

public class OnSameTeamCondition extends Condition {
	
	@Override
	public boolean setVar(String var) {
		return true;
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) {
			Team team1 = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
			Team team2 = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam((Player)target);
			return team1 != null && team2 != null && team1.equals(team2);
		} else {
			return false;
		}
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}
	
}
