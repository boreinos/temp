package com.afilon.universalprintservice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by ccano on 2/20/2016.
 */

public class PrintServiceXP<T, E> extends PrintDocumentAdapter {
    /**
     * Row: mapped to a flag value that is <code>true</code> if
     * the current layout is for a print preview, <code>false</code> otherwise.
     * This extra is provided in the {@link Bundle} argument of the {@link
     * #onLayout(PrintAttributes, PrintAttributes, CancellationSignal,
     * LayoutResultCallback, Bundle)} callback.
     *
     * @see #onLayout(PrintAttributes, PrintAttributes, CancellationSignal,
     * LayoutResultCallback, Bundle)
     */
    public static final String ROW_PRINT_CONTROL = "ROW_PRINT_CONTROL";

    private final T key;
    private final E value;

    private Context context;


    public PrintServiceXP(Context context, T key, E value) {
        this.context = context;
        this.key = key;
        this.value = value;
    }

    public T getKey() { return this.key; }

    public E getValue() { return this.value; }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

    }
}