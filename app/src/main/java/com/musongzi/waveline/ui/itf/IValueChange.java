package com.musongzi.waveline.ui.itf;

public interface IValueChange<I> {

    I onValueChange(I value, I lastValues, int index, int size);

}
