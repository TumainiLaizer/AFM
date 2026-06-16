package com.fameafrica.afm.ui.screen.career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.model.CareerSaveModel
import com.fameafrica.afm.domain.manager.CareerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val careerManager: CareerManager
) : ViewModel() {

    private val _savedCareers = MutableStateFlow<List<CareerSaveModel>>(emptyList())
    val savedCareers: StateFlow<List<CareerSaveModel>> = _savedCareers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSavedCareers()
    }

    fun loadSavedCareers() {
        viewModelScope.launch {
            _isLoading.value = true
            val careers = careerManager.listCareers()
            _savedCareers.value = careers
            _isLoading.value = false
        }
    }

    fun deleteCareer(careerId: Int) {
        viewModelScope.launch {
            careerManager.deleteCareer(careerId)
            loadSavedCareers()
        }
    }
}