package ru.constant.kidhealth.database.converter;

import com.raizlabs.android.dbflow.converter.TypeConverter;

import org.joda.time.DateTime;



@com.raizlabs.android.dbflow.annotation.TypeConverter
public class DateTimeConverter extends TypeConverter<Long, DateTime> {

    @Override
    public Long getDBValue(DateTime model) {
        return model == null ? null : model.toDate().getTime();
    }

    @Override
    public DateTime getModelValue(Long data) {
        return data == null ? null : new DateTime(data);
    }
}
