package org.blueseacode.ai.faq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ChatFaq;
import org.blueseacode.dao.mapper.ChatFaqMapper;
import org.blueseacode.dao.util.PageUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final ChatFaqMapper faqMapper;

    /** 根据问题匹配FAQ（精确匹配 → 模糊匹配） */
    public List<ChatFaq> matchByKeyword(String question) {
        // 1. 精确匹配
        List<ChatFaq> exact = faqMapper.selectList(
                new LambdaQueryWrapper<ChatFaq>()
                        .eq(ChatFaq::getStatus, 1)
                        .eq(ChatFaq::getQuestion, question));
        if (!exact.isEmpty()) return exact;

        // 2. 模糊匹配（LIKE 问题或答案）
        return faqMapper.selectList(
                new LambdaQueryWrapper<ChatFaq>()
                        .eq(ChatFaq::getStatus, 1)
                        .and(w -> w.like(ChatFaq::getQuestion, question)
                                .or().like(ChatFaq::getAnswer, question))
                        .last("LIMIT 5"));
    }

    /** 分页查询FAQ（管理端使用） */
    public PageResult<ChatFaq> list(String keyword, String category, int page, int size) {
        Page<ChatFaq> p = faqMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ChatFaq>()
                        .like(keyword != null && !keyword.isEmpty(), ChatFaq::getQuestion, keyword)
                        .eq(category != null && !category.isEmpty(), ChatFaq::getCategory, category)
                        .orderByAsc(ChatFaq::getSort)
                        .orderByDesc(ChatFaq::getCreateTime));
        return PageUtil.of(p);
    }

    /** 新增FAQ */
    public void create(ChatFaq faq) {
        faq.setHitCount(0);
        faqMapper.insert(faq);
    }

    /** 更新FAQ */
    public void update(ChatFaq faq) {
        faqMapper.updateById(faq);
    }

    /** 删除FAQ */
    public void delete(Long id) {
        faqMapper.deleteById(id);
    }

    /** 命中计数+1 */
    public void incrementHit(Long id) {
        faqMapper.update(null, new LambdaUpdateWrapper<ChatFaq>()
                .eq(ChatFaq::getId, id)
                .setSql("hit_count = hit_count + 1"));
    }
}
