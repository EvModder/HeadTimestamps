package net.evmodder.HeadTimestamps;

final class ClockEmojiUtil{
	static String getClockEmoji(int h, int m){
		switch(h%12){
			case 0:
				return m < 30 ? "🕛" : "🕧";
			case 1:
				return m < 30 ? "🕐" : "🕜";
			case 2:
				return m < 30 ? "🕑" : "🕝";
			case 3:
				return m < 30 ? "🕒" : "🕞";
			case 4:
				return m < 30 ? "🕓" : "🕟";
			case 5:
				return m < 30 ? "🕔" : "🕠";
			case 6:
				return m < 30 ? "🕕" : "🕡";
			case 7:
				return m < 30 ? "🕖" : "🕢";
			case 8:
				return m < 30 ? "🕗" : "🕣";
			case 9:
				return m < 30 ? "🕘" : "🕤";
			case 10:
				return m < 30 ? "🕙" : "🕥";
			case 11:
				return m < 30 ? "🕚" : "🕦";
		}
		return "()";
	}
}