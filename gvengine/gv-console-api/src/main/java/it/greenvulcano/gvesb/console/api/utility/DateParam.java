package it.greenvulcano.gvesb.console.api.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class DateParam {
	private final Date date;

	public DateParam(String dateStr) throws WebApplicationException {
		if (Utility.isEmpty(dateStr)) {
			this.date = null;
			return;
		}
		final DateFormat dateFormat = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);
		try {
			this.date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("Couldn't parse date string: " + e.getMessage())
					.build());
		}
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DateParam [date=");
		builder.append(date);
		builder.append("]");
		return builder.toString();
	}

}
