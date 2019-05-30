package com.togocourier.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.togocourier.R
import com.togocourier.adapter.ChatListAdapter
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var chatListAdapter: ChatListAdapter? = null
    private var histortList = ArrayList<Chat>()
    private var userList = ArrayList<UserInfoFCM>()
    private var map = HashMap<String, Chat>()
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        progress = ProgressDialog(context!!)
        userList= ArrayList()
        chatListAdapter = ChatListAdapter(context!!, histortList)
        val layoutManager = LinearLayoutManager(context)
        view.recycler_view.layoutManager = layoutManager

        view.recycler_view.adapter = chatListAdapter
        getChatHistory(view)
        return view
    }

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    private fun getChatHistory(view: View) {
        if (Constant.isNetworkAvailable(context!!, view)) {
            // view.progressBar_chat.visibility = View.VISIBLE
            progress!!.show()
            val myUid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")

            FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value == null) {
                        // view.progressBar_chat.visibility = View.GONE
                        progress!!.dismiss()
                        view.no_chat_found.visibility = View.VISIBLE
                    }
                }
            })




            FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid).addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildRemoved(p0: DataSnapshot) {
                    val key = p0.key
                    val iter = histortList.iterator()
                    while (iter.hasNext()) {
                        val str = iter.next()

                        if (str.uid == key)
                            iter.remove()
                        map.remove(key)

                        if (histortList.size == 0) {
                            view.no_chat_found.visibility = View.VISIBLE
                        } else {
                            view.no_chat_found.visibility = View.GONE
                        }
                    }
                    chatListAdapter?.notifyDataSetChanged()
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    val chat = p0.getValue(Chat::class.java)!!
                    val key = p0.key

                    gettingDataFromUserTable(view, key!!, chat)
                    view.no_chat_found.visibility = View.GONE
                    // view.progressBar_chat.visibility = View.GONE
                    progress!!.dismiss()
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chat = p0.getValue(Chat::class.java)!!
                    val key = p0.key

                    gettingDataFromUserTable(view, key!!, chat)
                    view.no_chat_found.visibility = View.GONE
                    // view.progressBar_chat.visibility = View.GONE
                    progress!!.dismiss()
                }
            })
        } else {
            progress!!.dismiss()
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun gettingDataFromUserTable(view: View, id: String, chat: Chat) {

        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(UserInfoFCM::class.java)
                userList.add(user!!)
                histortList.clear()

                for (userValue in userList) {
                    if (userValue.uid == id) {
                        chat.profilePic = userValue.profilePic
                        chat.name = userValue.name
                        chat.firebaseToken = userValue.firebaseToken
                        chat.uid = id
                        chat.timestamp = chat.timestamp

                    }
                }
                map.put(chat.uid, chat)

                val demoValues: Collection<Chat> = map.values
                //histortList = ArrayList(demoValues)
                histortList.addAll(demoValues)


                /*var isExist=false
                if (histortList.isEmpty()) {
                    chat.profilePic = user!!.profilePic
                    chat.name = user.name
                    chat.firebaseToken = user.firebaseToken
                    histortList.add(chat)
                } else {
                    for (history in histortList) {

                        if (history.uid == id) {
                            history.profilePic = user!!.profilePic
                            history.name = user.name
                            history.firebaseToken = user.firebaseToken
                            isExist=true
                            break
                        }
                    }
                    if (!isExist){
                        chat.profilePic = user!!.profilePic
                        chat.name = user.name
                        chat.firebaseToken = user.firebaseToken
                        histortList.add(chat)
                    }
                }*/

/*
                    chatListAdapter = ChatListAdapter(context!!, histortList)
                    val layoutManager = LinearLayoutManager(context)

                    view.recycler_view.layoutManager = layoutManager
                    view.recycler_view.adapter = chatListAdapter
*/
                    chatListAdapter?.notifyDataSetChanged()


                shortList()
            }
        })
    }

    private fun shortList() {
        Collections.sort(histortList) { a1, a2 ->
            if (a1!!.timestamp == null || a2!!.timestamp == null)
                -1
            else {
                val long1: Long = a1.timestamp as Long
                val long2: Long = a2.timestamp as Long
                long2.compareTo(long1)
            }
        }
        chatListAdapter?.notifyDataSetChanged()
    }


    override fun onResume() {
        super.onResume()
        userList.clear()
    }



}
