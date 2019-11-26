(ns semente.prosemirror.subscription
  (:require
   [applied-science.js-interop :as j]
   [re-frame.core :as rf]
   [semente.prosemirror.util :refer (mark-type)]
   ["prosemirror-commands" :refer (toggleMark)]
   ["prosemirror-state" :refer (EditorState)]))


(rf/reg-sub
 :editor-state
 (fn [db _]
   (:editor-state db)))

(rf/reg-sub
 :mark-available
 :<- [:editor-state]
 (fn [^EditorState editor-state [_ mark-name]]
   ((toggleMark (mark-type editor-state mark-name))
    editor-state)))

(rf/reg-sub
 :mark-active
 :<- [:editor-state]
 (fn [^EditorState editor-state [_ mark-name]]
   (let [mt (mark-type editor-state mark-name)
         {:keys [from $from to empty]} (j/lookup (j/get editor-state :selection))]
     (if empty
       (boolean (j/call mt :isInSet (or (j/get editor-state :storedMarks)
                                        (j/call $from :marks))))
       (j/call-in editor-state [:doc :rangeHasMark] from to mt)))))