package com.ivanmorgillo.messagist

import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ivanmorgillo.messagist.helpers.ScopedActivity
import com.ivanmorgillo.messagist.helpers.exhaustive
import com.ivanmorgillo.messagist.messages.MessageAdapter
import com.ivanmorgillo.messagist.messages.MessageListEvent
import com.ivanmorgillo.messagist.messages.MessageListEvent.*
import com.ivanmorgillo.messagist.messages.MessageListState.ShowMessages
import com.ivanmorgillo.messagist.messages.MessageListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class MainActivity : ScopedActivity() {
    private val viewmodel: MessageListViewModel by viewModel()

    private val messagesAdapter = MessageAdapter(
        bottomReached = {
            Timber.d("Bottom reached \uD83D\uDC4D")
            viewmodel.send(OnListBottomReached)
        },
        onClick = {
            Timber.d("Attachment clicked \uD83D\uDC4D")
            viewmodel.send(MessageListEvent.OnAttachmentClicked(it))
        })

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messagesAdapter.setHasStableIds(true)
        message_list.adapter = messagesAdapter

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(message_list)

        activityScope.launch {
            viewmodel.states.consumeEach { state ->
                when (state) {
                    is ShowMessages -> messagesAdapter.setItems(state.messages)
                }.exhaustive
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.send(OnResume)
    }


    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                viewmodel.send(OnElementAtPositionSwiped(position))
            }
        }
}
