/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.home.createdirect

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProviders
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.jakewharton.rxbinding3.widget.textChanges
import im.vector.matrix.android.api.session.user.model.User
import im.vector.riotx.R
import im.vector.riotx.core.extensions.hideKeyboard
import im.vector.riotx.core.extensions.setupAsSearch
import im.vector.riotx.core.platform.VectorBaseFragment
import kotlinx.android.synthetic.main.fragment_create_direct_room_directory_users.*
import javax.inject.Inject

class CreateDirectRoomDirectoryUsersFragment @Inject constructor(
        private val directRoomController: DirectoryUsersController
) : VectorBaseFragment(), DirectoryUsersController.Callback {

    override fun getLayoutResId() = R.layout.fragment_create_direct_room_directory_users

    private val viewModel: CreateDirectRoomViewModel by activityViewModel()

    private lateinit var actionViewModel: CreateDirectRoomSharedActionViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        actionViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(CreateDirectRoomSharedActionViewModel::class.java)
        setupRecyclerView()
        setupSearchByMatrixIdView()
        setupCloseView()
    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        directRoomController.callback = this
        recyclerView.setController(directRoomController)
    }

    private fun setupSearchByMatrixIdView() {
        createDirectRoomSearchById.setupAsSearch(searchIconRes = 0)
        createDirectRoomSearchById
                .textChanges()
                .subscribe {
                    viewModel.handle(CreateDirectRoomAction.SearchDirectoryUsers(it.toString()))
                }
                .disposeOnDestroy()
        createDirectRoomSearchById.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(createDirectRoomSearchById, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupCloseView() {
        createDirectRoomClose.setOnClickListener {
            actionViewModel.post(CreateDirectRoomSharedAction.GoBack)
        }
    }

    override fun invalidate() = withState(viewModel) {
        directRoomController.setData(it)
    }

    override fun onItemClick(user: User) {
        view?.hideKeyboard()
        viewModel.handle(CreateDirectRoomAction.SelectUser(user))
        actionViewModel.post(CreateDirectRoomSharedAction.GoBack)
    }

    override fun retryDirectoryUsersRequest() {
        val currentSearch = createDirectRoomSearchById.text.toString()
        viewModel.handle(CreateDirectRoomAction.SearchDirectoryUsers(currentSearch))
    }
}
