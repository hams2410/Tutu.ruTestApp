package ru.pro2410.tutu;

import android.app.DatePickerDialog;
import android.app.Dialog;
import java.util.Calendar;
import android.os.Bundle;

import android.app.DialogFragment;
import android.widget.Button;
import android.widget.DatePicker;

public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    Button button;
    public DateDialog(Button view) {
        button = view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(),this, year,month,day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date =  dayOfMonth+"-"+(month+1)+"-"+year;
        button.setText(date);
    }
}
