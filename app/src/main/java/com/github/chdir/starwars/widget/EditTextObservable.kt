package com.github.chdir.starwars.widget;

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.rxjava3.core.Observable

public class EditTextObservable private constructor() {
    companion object {
        fun create(editText: EditText) : Observable<String> {
            return Observable.create { emitter ->
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        emitter.onNext(s.toString())
                    }
                })
            }
        }
    }
}
