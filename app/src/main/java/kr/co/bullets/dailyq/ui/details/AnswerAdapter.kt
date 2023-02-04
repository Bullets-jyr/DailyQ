package kr.co.bullets.dailyq.ui.details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.bullets.dailyq.api.response.Answer
import kr.co.bullets.dailyq.databinding.ItemAnswerBinding

// AnswerViewHolder를 사용하는 AnswerAdapter를 만듭니다. 답의 개수를 예측할 수 없기 때문에
// 페이징해서 가져올 수 있도록 PagingDataAdapter를 사용해야 하지만, 이미 타임라인에서 PagingDataAdapter를 학습했고
// 더미 데이터에는 입력된 답이 몇개 없어 모든 데이터를 받아와 한 번에 표시하겠습니다.
class AnswerAdapter(context: Context) : RecyclerView.Adapter<AnswerViewHolder>() {

    val inflater = LayoutInflater.from(context)

    var items: List<Answer>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        return AnswerViewHolder(ItemAnswerBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }
}