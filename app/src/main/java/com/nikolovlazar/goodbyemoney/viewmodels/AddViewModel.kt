package com.nikolovlazar.goodbyemoney.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolovlazar.goodbyemoney.db
import com.nikolovlazar.goodbyemoney.models.Category
import com.nikolovlazar.goodbyemoney.models.Expense
import com.nikolovlazar.goodbyemoney.models.Recurrence
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import android.content.Context
import android.util.Log
import com.nikolovlazar.goodbyemoney.ml.TransactionClassifier
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class AddScreenState(
  val amount: String = "",
  val recurrence: Recurrence = Recurrence.None,
  val date: LocalDate = LocalDate.now(),
  val note: String = "",
  val remark: String = "",
  val category: Category? = null,
  val categories: RealmResults<Category>? = null
)

class Tokenizer(context: Context, vocabFileName: String) {
  private val wordIndex = mutableMapOf<String, Int>()

  init {
    val reader = BufferedReader(InputStreamReader(context.assets.open(vocabFileName)))
    reader.useLines { lines ->
      lines.forEachIndexed { index, line ->
        wordIndex[line.trim()] = index + 1
      }
    }
  }

  fun textsToSequences(texts: List<String>): List<List<Int>> {
    return texts.map { text ->
      text.split(" ", "/").mapNotNull { word ->
        wordIndex[word.lowercase()]
      }
    }
  }
}

fun padSequences(sequences: List<List<Int>>, maxLength: Int): Array<IntArray> {
  return Array(sequences.size) { i ->
    val sequence = sequences[i]
    val padded = IntArray(maxLength)
    for (j in sequence.indices) {
      if (j >= maxLength) break
      padded[j] = sequence[j]
    }
    padded
  }
}

fun convertToByteBuffer(paddedSequences: Array<IntArray>, maxLength: Int): Buffer {
  val inputBuffer = ByteBuffer.allocateDirect(4 * maxLength).order(ByteOrder.nativeOrder())
  for (sequence in paddedSequences) {
    for (token in sequence) {
      inputBuffer.putFloat(token.toFloat())
    }
  }
  return inputBuffer.rewind()
}

class AddViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(AddScreenState())
  private lateinit var tokenizer: Tokenizer
  val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

  init {
    _uiState.update { currentState ->
      currentState.copy(
        categories = db.query<Category>().find()
      )
    }
  }

  fun setAmount(amount: String) {
    var parsed = amount.toDoubleOrNull()

    if (amount.isEmpty()) {
      parsed = 0.0
    }

    if (parsed != null) {
      _uiState.update { currentState ->
        currentState.copy(
          amount = amount.trim().ifEmpty { "0" },
        )
      }
    }
  }

  fun setRecurrence(recurrence: Recurrence) {
    _uiState.update { currentState ->
      currentState.copy(
        recurrence = recurrence,
      )
    }
  }

  fun setDate(date: LocalDate) {
    _uiState.update { currentState ->
      currentState.copy(
        date = date,
      )
    }
  }

  fun setNote(note: String) {
    _uiState.update { currentState ->
      currentState.copy(
        note = note,
      )
    }
  }

  fun setRemark(remark: String) {
    _uiState.update { currentState ->
      currentState.copy(
        remark = remark,
      )
    }
  }

  fun setCategory(category: Category) {
    _uiState.update { currentState ->
      currentState.copy(
        category = category,
      )
    }
  }

  fun submitExpense(context: Context) {
    if (_uiState.value.remark != "") {
      viewModelScope.launch(Dispatchers.IO) {
        tokenizer = Tokenizer(context, "vocab.txt")
        val labels = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(context.assets.open("labels.txt")))
        reader.useLines { lines ->
          lines.forEach { line ->
            labels.add(line.trim())
          }
        }
        val transactionDetail = _uiState.value.remark
        val inputSeq = tokenizer.textsToSequences(listOf(transactionDetail))
        val maxLength = 8
        val inputPadded = padSequences(inputSeq, maxLength)
        val inputBuffer = convertToByteBuffer(inputPadded, maxLength)

        val model = TransactionClassifier.newInstance(context)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, maxLength), DataType.FLOAT32)
        inputFeature0.loadBuffer(inputBuffer as ByteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        model.close()

        val maxIndex = outputFeature0.floatArray.indices.maxByOrNull { outputFeature0.floatArray[it] } ?: -1
        val now = LocalDateTime.now()
        db.write {
          this.copyToRealm(
            Expense(
              _uiState.value.amount.toDouble(),
              _uiState.value.recurrence,
              _uiState.value.date.atTime(now.hour, now.minute, now.second),
              _uiState.value.note,
              this.query<Category>("name == $0", labels[maxIndex])
                .find().first(),
            )
          )
        }
        _uiState.update { currentState ->
          currentState.copy(
            amount = "",
            recurrence = Recurrence.None,
            date = LocalDate.now(),
            note = "",
            remark = "",
            category = null,
            categories = null
          )
        }
      }
    }
  }
}