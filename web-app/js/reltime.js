var RelTime =  function( time ) {
	this.time = time;
}

RelTime.duration = {
	s: 1,
	m: 60,
	h: 3600,
	d: 86400,
	w: 604800,
	y: 365 * 86400
};

RelTime.fromDates = function( dateStart, dateEnd ) {
	if( !( dateStart instanceof Date ) || !( dateEnd instanceof Date ) ) {
		return null;
	}
	
	return new RelTime( ( dateEnd.getTime() - dateStart.getTime() ) / 1000 );
}

RelTime.prototype = {
	/**
	 * Round this relative time to the given duration. E.g. 'y' means
	 * round down to whole years.
	 * Specify step to round to a multiple of the scale. E.g. roundDown( 'y', 5 ) means 
	 * rounding down to multiples of 5 years 
	 */
	roundDown: function( to, step ) {
		var scale = RelTime.duration[ to ];
		
		// If we don't have a scale to work with, return immediately
		if( !scale )
			return;
		
		if( step )
			scale *= step;
		
		var remainder = this.time % scale; 
		
		if( remainder == 0 ) {
			// Already rounded; do nothing
			return;
		}
		
		// If lower than 0, rounding down works differently
		if( this.time > 0 ) {
			this.time = this.time - remainder; 
		} else {
			// e.g. round down - 2.5 to scale 3:
			// 2.5 - ( 3 + -2.5) = -3
			this.time = this.time - ( scale + remainder );
		}
	},
	
	add: function( scale, number ) {
		scale = RelTime.duration[ scale ];
		
		if( !scale )
			return;
		
		this.time += number * scale;
	},
	
	/**
	 * Rounds this relative time to the nearest value
	 */
	round: function( to, step ) {
		var scale = RelTime.duration[ to ];
		
		// If we don't have a scale to work with, return immediately
		if( !scale )
			return;
		
		if( step )
			scale *= step;
		
		this.time = Math.round( this.time /  scale )  * scale;
		
	},
	
	/**
	 * Returns a string representation of a specific part of this reltime. E.g. 
	 * a reltime of 1w 3d 2h 1s will result in 3d if requested with 'd'
	 */
	getModulo: function( to ) {
		switch( to ) {
			case 'y': return new RelTime( Math.round( this.time / RelTime.duration.y )														* RelTime.duration.y );
			case 'w': return new RelTime( Math.round( ( this.time % RelTime.duration.y ) / RelTime.duration.w )								* RelTime.duration.w );
			case 'd': return new RelTime( Math.round( ( ( this.time % RelTime.duration.y ) % RelTime.duration.w ) / RelTime.duration.d )	* RelTime.duration.d );
			case 'h': return new RelTime( Math.round( ( this.time % RelTime.duration.d ) / RelTime.duration.h )								* RelTime.duration.h );
			case 'm': return new RelTime( Math.round( ( this.time % RelTime.duration.h ) / RelTime.duration.m )								* RelTime.duration.m );
			case 's': return new RelTime( Math.round( ( this.time % RelTime.duration.m ) / RelTime.duration.s )								* RelTime.duration.s );
			default: return null;
		}
	},
		
	// Returns a string representation of this relative time
	toString: function() {
		var negative = this.time < 0;
		var reltime = Math.abs( this.time );
		var remaining = reltime;
		
		var years 	= Math.floor(remaining / RelTime.duration.y); remaining -= years * RelTime.duration.y;
		var weeks	= Math.floor(remaining / RelTime.duration.w); remaining -= weeks * RelTime.duration.w; 
		var days	= Math.floor(remaining / RelTime.duration.d); remaining -= days * RelTime.duration.d;
		var hours 	= Math.floor(remaining / RelTime.duration.h); remaining -= hours * RelTime.duration.h;
		var minutes	= Math.floor(remaining / RelTime.duration.m); remaining -= minutes * RelTime.duration.m;
		var seconds	= Math.floor(remaining / RelTime.duration.s); remaining -= seconds * RelTime.duration.s;
	
		var stringValue = negative ? "-" : "";
		if (years > 0) { stringValue += years + "y "; }
		if (weeks > 0) { stringValue += weeks + "w "; }
		if (days > 0) { stringValue += days + "d "; }
		if (hours > 0) { stringValue += hours + "h "; }
		if (minutes > 0) { stringValue += minutes + "m "; }
		if (seconds > 0) { stringValue += seconds + "s "; }
	
		if (reltime == 0) stringValue = "0";
	
		return $.trim( stringValue );
	},
	
	
	
}

