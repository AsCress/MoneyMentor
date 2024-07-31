package com.nikolovlazar.goodbyemoney.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolovlazar.goodbyemoney.db
import com.nikolovlazar.goodbyemoney.models.Category
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriesState(
  val newCategoryColor: Color = Color.White,
  val newCategoryName: String = "",
  val colorPickerShowing: Boolean = false,
  val categories: List<Category> = listOf()
)

class CategoriesViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(CategoriesState())
  val uiState: StateFlow<CategoriesState> = _uiState.asStateFlow()

  init {
    _uiState.update { currentState ->
      currentState.copy(
        categories = db.query<Category>().find()
      )
    }
    viewModelScope.launch(Dispatchers.IO) {
      if(db.query<Category>().count().find() == 0L) {
        db.write {
          this.copyToRealm(
            Category(
              "Banking & Finance",
              Color.White
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Business Expenses",
              Color.Red
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Cash Withdrawals",
              Color.Blue
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Entertainment & Leisure",
              Color.Cyan
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Food & Dining",
              Color.Gray
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Healthcare",
              Color.Green
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Housing & Utilities",
              Color.Magenta
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Income",
              Color.Yellow
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Other",
              Color.LightGray
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Shopping",
              Color.DarkGray
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Taxes & Government",
              Color(50, 25, 25)
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Transportation",
              Color(50, 75, 25)
            )
          )
        }
        db.write {
          this.copyToRealm(
            Category(
              "Travel",
              Color(50, 25, 75)
            )
          )
        }
      }
      db.query<Category>().asFlow().collect { changes ->
        _uiState.update { currentState ->
          currentState.copy(
            categories = changes.list
          )
        }
      }
    }
  }

  fun setNewCategoryColor(color: Color) {
    _uiState.update { currentState ->
      currentState.copy(
        newCategoryColor = color
      )
    }
  }

  fun setNewCategoryName(name: String) {
    _uiState.update { currentState ->
      currentState.copy(
        newCategoryName = name
      )
    }
  }

  fun showColorPicker() {
    _uiState.update { currentState ->
      currentState.copy(
        colorPickerShowing = true
      )
    }
  }

  fun hideColorPicker() {
    _uiState.update { currentState ->
      currentState.copy(
        colorPickerShowing = false
      )
    }
  }

  fun createNewCategory() {
    viewModelScope.launch(Dispatchers.IO) {
      db.write {
        this.copyToRealm(Category(
          _uiState.value.newCategoryName,
          _uiState.value.newCategoryColor
        ))
      }
      _uiState.update { currentState ->
        currentState.copy(
          newCategoryColor = Color.White,
          newCategoryName = ""
        )
      }
    }
  }

  fun deleteCategory(category: Category) {
    viewModelScope.launch(Dispatchers.IO) {
      db.write {
        val deletingCategory = this.query<Category>("_id == $0", category._id).find().first()
        delete(deletingCategory)
      }
    }
  }
}