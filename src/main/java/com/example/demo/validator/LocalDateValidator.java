package com.example.demo.validator;

import java.time.LocalDate;
import com.example.demo.exception.LocalDateException;
import org.springframework.stereotype.Component;

@Component
public class LocalDateValidator {

    public void isValid(LocalDate localDate) throws LocalDateException {
        LocalDate currentDate = LocalDate.now();
        LocalDate minDate = currentDate.minusYears(10);
        LocalDate maxDate = currentDate.minusYears(100);
        if(localDate.isBefore(minDate) && localDate.isAfter(maxDate)) {
        } else{
            throw new LocalDateException("Wrong Date Of Birth");
        }

    }
}
