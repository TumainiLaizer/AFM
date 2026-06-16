package com.fameafrica.afm.ui.screen.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.data.repository.StaffRepository
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffDetailUiState(
    val staff: StaffEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currencyContext: CurrencyFormatter.CurrencyContext? = null
)

@HiltViewModel
class StaffDetailViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val currencyFormatter: CurrencyFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffDetailUiState())
    val uiState: StateFlow<StaffDetailUiState> = _uiState.asStateFlow()

    fun loadStaff(staffId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val staff = staffRepository.getStaffById(staffId)
            
            val currencyContext = currencyFormatter.getCurrentContext()

            _uiState.update { it.copy(
                staff = staff,
                isLoading = false,
                currencyContext = currencyContext
            ) }
        }
    }

    fun terminateContract() {
        val staff = _uiState.value.staff ?: return
        viewModelScope.launch {
            staffRepository.deleteStaff(staff)
            // Navigation handled by screen
        }
    }
}
